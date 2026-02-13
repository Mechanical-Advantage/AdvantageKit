// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include <frc/RobotBase.h>
#include <frc/RobotController.h>
#include <frc/Timer.h>
#include "akit/Logger.h"
#include "akit/LoggedDriverStation.h"
#include "akit/LoggedSystemStats.h"
#include "akit/LoggedPowerDistribution.h"
#include "akit/RadioLogger.h"
#include "akit/conduit/ConduitApi.h"

using namespace akit;

bool Logger::running = false;
long Logger::cycleCount = 0;
LogTable Logger::entry { 0_s };
std::mutex Logger::entryMutex;
std::optional<LogTable> Logger::outputTable;
std::unordered_map<std::string, std::string> Logger::metadata;
std::unique_ptr<ConsoleSource> Logger::console;
std::vector<akit::nt::LoggedNetworkInput*> Logger::dashboardInputs;
bool Logger::enableConsole = true;
bool Logger::checkRobotBase = true;
std::unique_ptr<LogReplaySource> Logger::replaySource;
moodycamel::BlockingReaderWriterQueue<LogTable> Logger::receiverQueue {
		Logger::RECEIVER_QUEUE_CAPACITY };
std::unique_ptr<ReceiverThread> Logger::receiverThread;
bool Logger::receiverQueueFault = false;

void Logger::setReplaySource(std::unique_ptr<LogReplaySource> replaySource) {
	if (!running)
		Logger::replaySource = std::move(replaySource);
}

void Logger::addDataReceiver(std::unique_ptr<LogDataReceiver> dataReceiver) {
	if (!running)
		receiverThread->addDataReceiver(std::move(dataReceiver));
}

void Logger::registerDashboardInput(
		akit::nt::LoggedNetworkInput &dashboardInput) {
	dashboardInputs.push_back(&dashboardInput);
}

void Logger::recordMetadata(std::string key, std::string value) {
	if (!running)
		metadata.insert( { key, value });
}

void Logger::start() {
	if (!running) {
		running = true;

		if (checkRobotBase) {
			// TODO
		}

		if (replaySource) {
			const char *halSimEnv = std::getenv("HALSIM_EXTENSIONS");
			if (halSimEnv != NULL && std::strlen(halSimEnv) > 0) {
				FRC_ReportError(frc::err::IncompatibleMode,
						"All HAL simulation extensions must be disabled when running AdvantageKit replay, including the simulation GUI and DriverStation connection. Check the configuration in \"build.gradle\" and ensure that all checkboxes are disabled in the VSCode simulation popup.\n\n*** EXITING DUE TO INVALID SIMULATION CONFIGURATION, SEE ABOVE. ***");
				std::exit(1);
			}
		}

		if (enableConsole &&console) {
			if (frc::RobotBase::IsReal())
				console = std::make_unique<RoboRIOConsoleSource>();
			else
				console = std::make_unique<SimulatorConsoleSource>();
		}

		if (replaySource)
			replaySource->start();

		if (!replaySource)
			outputTable = entry.getSubtable("RealOutputs");
		else
			outputTable = entry.getSubtable("ReplayOutputs");

		LogTable metadataTable = entry.getSubtable(
				replaySource ? "RealMetadata" : "ReplayMetadata");
		for (auto &entry : metadata)
			metadataTable.put(entry.first, entry.second);

		receiverThread = std::make_unique < ReceiverThread > (receiverQueue);

		frc::RobotController::SetTimeSource([] {
			return units::microsecond_t { getTimestamp() }.value();
		});

		periodicBeforeUser();
	}
}

void Logger::end() {
	if (running) {
		running = false;
		console.release();

		replaySource.release();
		receiverThread.release();
		frc::RobotController::SetTimeSource(frc::RobotController::GetFPGATime);
	}
}

void Logger::periodicBeforeUser() {
	cycleCount++;
	if (running) {
		units::second_t entryUpdateStart = frc::Timer::GetFPGATimestamp();
		if (!replaySource) {
			std::lock_guard lock { entryMutex };
			entry.setTimestamp(frc::Timer::GetFPGATimestamp());
		} else {
			if (!replaySource->updateTable(entry)) {
				end();
				std::exit(1);
			}
		}

		units::millisecond_t dsStart = frc::Timer::GetFPGATimestamp();
		if (hasReplaySource())
			LoggedDriverStation::replayFromLog(
					entry.getSubtable("DriverStation"));

		units::millisecond_t dashboardInputsStart =
				frc::Timer::GetFPGATimestamp();
		for (auto &input : dashboardInputs)
			input->periodic();
		units::millisecond_t dashboardInputsEnd =
				frc::Timer::GetFPGATimestamp();

		recordOutput("Logger/EntryUpdateMS", dsStart - entryUpdateStart);
		if (hasReplaySource())
			recordOutput("Logger/DriverStationMS",
					dashboardInputsStart - dsStart);
		recordOutput("Logger/DashboardInputsMS",
				dashboardInputsEnd - dashboardInputsStart);
	}
}

void Logger::periodicAfterUser(units::millisecond_t userCodeLength,
		units::millisecond_t periodicBeforeLength,
		std::string extraConsoleData) {
	if (running) {
		conduit::ConduitApi &inst = conduit::ConduitApi::getInstance();
		units::millisecond_t conduitCaptureStart =
				frc::Timer::GetFPGATimestamp();
		inst.captureData();

		units::millisecond_t dsStart = frc::Timer::GetFPGATimestamp();
		if (!hasReplaySource())
			LoggedDriverStation::saveToLog(entry.getSubtable("DriverStation"));

		units::millisecond_t conduitSaveStart = frc::Timer::GetFPGATimestamp();
		if (!hasReplaySource()) {
			LoggedSystemStats::saveToLog(entry.getSubtable("SystemStats"));
			LoggedPowerDistribution &loggedPowerDistribution =
					LoggedPowerDistribution::getInstance();
			loggedPowerDistribution.saveToLog(
					entry.getSubtable("PowerDistribution"));
		}

		units::millisecond_t autoLogStart = frc::Timer::GetFPGATimestamp();
		units::millisecond_t alertLogStart = frc::Timer::GetFPGATimestamp();

		units::millisecond_t radioLogStart = frc::Timer::GetFPGATimestamp();
		if (!hasReplaySource())
			RadioLogger::periodic(entry.getSubtable("RadioStatus"));

		units::millisecond_t consoleCaptureStart =
				frc::Timer::GetFPGATimestamp();
		if (enableConsole) {
			std::string consoleData = console->getNewData();
			consoleData += extraConsoleData;
			if (!consoleData.empty())
				recordOutput("Console", consoleData);
		}
		units::millisecond_t consoleCaptureEnd = frc::Timer::GetFPGATimestamp();

		recordOutput("Logger/ConduitCaptureMS", dsStart - conduitCaptureStart);
		if (!hasReplaySource())
			recordOutput("Logger/DriverStationMS", conduitSaveStart - dsStart);
		recordOutput("Logger/ConduitSaveMS", autoLogStart - conduitSaveStart);
		recordOutput("Logger/AutoLogMS", alertLogStart - autoLogStart);
		recordOutput("Logger/AlertLogMS", radioLogStart - alertLogStart);
		recordOutput("Logger/RadioLogMS", consoleCaptureStart - radioLogStart);
		recordOutput("Logger/ConsoleMS",
				consoleCaptureEnd - consoleCaptureStart);
		units::millisecond_t periodicAfterLength = consoleCaptureEnd
				- conduitCaptureStart;
		recordOutput("LoggedRobot/LogPeriodicMS",
				periodicBeforeLength + periodicAfterLength);
		recordOutput("LoggedRobot/FullCycleMS",
				periodicBeforeLength + userCodeLength + periodicAfterLength);
		recordOutput("Logger/QueuedCycles", receiverQueue.size_approx());

		receiverQueueFault = !receiverQueue.try_enqueue(entry);
		if (receiverQueueFault)
			FRC_ReportError(frc::err::Error,
					"[AdvantageKit] Capacity of receiver queue exceeded, data will NOT be logged");
	}
}

units::second_t Logger::getTimestamp() {
	if (!running)
		return frc::Timer::GetFPGATimestamp();
	std::lock_guard lock { entryMutex };
	return entry.getTimestamp();
}

void Logger::runEveryN(size_t n, std::function<void()> function) {
	if (cycleCount % n == 0)
		function();
}

void Logger::processInputs(std::string key, inputs::LoggableInputs &inputs) {
	if (running) {
		if (!replaySource)
			inputs.toLog(entry.getSubtable(key));
		else
			inputs.fromLog(entry.getSubtable(key));
	}
}
