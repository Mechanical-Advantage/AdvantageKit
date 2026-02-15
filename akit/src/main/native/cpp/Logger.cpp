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
moodycamel::BlockingConcurrentQueue<LogTable> Logger::receiverQueue {
		Logger::RECEIVER_QUEUE_CAPACITY };
std::unique_ptr<ReceiverThread> Logger::receiverThread;
bool Logger::receiverQueueFault = false;

void Logger::SetReplaySource(std::unique_ptr<LogReplaySource> replaySource) {
	if (!running)
		Logger::replaySource = std::move(replaySource);
}

void Logger::AddDataReceiver(std::unique_ptr<LogDataReceiver> dataReceiver) {
	if (!running)
		receiverThread->AddDataReceiver(std::move(dataReceiver));
}

void Logger::RegisterDashboardInput(
		akit::nt::LoggedNetworkInput &dashboardInput) {
	dashboardInputs.push_back(&dashboardInput);
}

void Logger::RecordMetadata(std::string key, std::string value) {
	if (!running)
		metadata.insert( { key, value });
}

void Logger::Start() {
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
			replaySource->Start();

		if (!replaySource)
			outputTable = entry.GetSubtable("RealOutputs");
		else
			outputTable = entry.GetSubtable("ReplayOutputs");

		LogTable metadataTable = entry.GetSubtable(
				replaySource ? "RealMetadata" : "ReplayMetadata");
		for (auto &entry : metadata)
			metadataTable.Put(entry.first, entry.second);

		receiverThread = std::make_unique < ReceiverThread > (receiverQueue);

		frc::RobotController::SetTimeSource([] {
			return units::microsecond_t { GetTimestamp() }.value();
		});

		PeriodicBeforeUser();
	}
}

void Logger::End() {
	if (running) {
		running = false;
		console.release();

		replaySource.release();
		receiverThread.release();
		frc::RobotController::SetTimeSource(frc::RobotController::GetFPGATime);
	}
}

void Logger::PeriodicBeforeUser() {
	cycleCount++;
	if (running) {
		units::second_t entryUpdateStart = frc::Timer::GetFPGATimestamp();
		if (!replaySource) {
			std::lock_guard lock { entryMutex };
			entry.SetTimestamp(frc::Timer::GetFPGATimestamp());
		} else {
			if (!replaySource->UpdateTable(entry)) {
				End();
				std::exit(1);
			}
		}

		units::millisecond_t dsStart = frc::Timer::GetFPGATimestamp();
		if (HasReplaySource())
			LoggedDriverStation::ReplayFromLog(
					entry.GetSubtable("DriverStation"));

		units::millisecond_t dashboardInputsStart =
				frc::Timer::GetFPGATimestamp();
		for (auto &input : dashboardInputs)
			input->Periodic();
		units::millisecond_t dashboardInputsEnd =
				frc::Timer::GetFPGATimestamp();

		RecordOutput("Logger/EntryUpdateMS", dsStart - entryUpdateStart);
		if (HasReplaySource())
			RecordOutput("Logger/DriverStationMS",
					dashboardInputsStart - dsStart);
		RecordOutput("Logger/DashboardInputsMS",
				dashboardInputsEnd - dashboardInputsStart);
	}
}

void Logger::PeriodicAfterUser(units::millisecond_t userCodeLength,
		units::millisecond_t periodicBeforeLength,
		std::string extraConsoleData) {
	if (running) {
		conduit::ConduitApi &inst = conduit::ConduitApi::getInstance();
		units::millisecond_t conduitCaptureStart =
				frc::Timer::GetFPGATimestamp();
		inst.captureData();

		units::millisecond_t dsStart = frc::Timer::GetFPGATimestamp();
		if (!HasReplaySource())
			LoggedDriverStation::SaveToLog(entry.GetSubtable("DriverStation"));

		units::millisecond_t conduitSaveStart = frc::Timer::GetFPGATimestamp();
		if (!HasReplaySource()) {
			LoggedSystemStats::SaveToLog(entry.GetSubtable("SystemStats"));
			LoggedPowerDistribution &loggedPowerDistribution =
					LoggedPowerDistribution::GetInstance();
			loggedPowerDistribution.SaveToLog(
					entry.GetSubtable("PowerDistribution"));
		}

		units::millisecond_t autoLogStart = frc::Timer::GetFPGATimestamp();
		units::millisecond_t alertLogStart = frc::Timer::GetFPGATimestamp();

		units::millisecond_t radioLogStart = frc::Timer::GetFPGATimestamp();
		if (!HasReplaySource())
			RadioLogger::Periodic(entry.GetSubtable("RadioStatus"));

		units::millisecond_t consoleCaptureStart =
				frc::Timer::GetFPGATimestamp();
		if (enableConsole) {
			std::string consoleData = console->GetNewData();
			consoleData += extraConsoleData;
			if (!consoleData.empty())
				RecordOutput("Console", consoleData);
		}
		units::millisecond_t consoleCaptureEnd = frc::Timer::GetFPGATimestamp();

		RecordOutput("Logger/ConduitCaptureMS", dsStart - conduitCaptureStart);
		if (!HasReplaySource())
			RecordOutput("Logger/DriverStationMS", conduitSaveStart - dsStart);
		RecordOutput("Logger/ConduitSaveMS", autoLogStart - conduitSaveStart);
		RecordOutput("Logger/AutoLogMS", alertLogStart - autoLogStart);
		RecordOutput("Logger/AlertLogMS", radioLogStart - alertLogStart);
		RecordOutput("Logger/RadioLogMS", consoleCaptureStart - radioLogStart);
		RecordOutput("Logger/ConsoleMS",
				consoleCaptureEnd - consoleCaptureStart);
		units::millisecond_t periodicAfterLength = consoleCaptureEnd
				- conduitCaptureStart;
		RecordOutput("LoggedRobot/LogPeriodicMS",
				periodicBeforeLength + periodicAfterLength);
		RecordOutput("LoggedRobot/FullCycleMS",
				periodicBeforeLength + userCodeLength + periodicAfterLength);
		RecordOutput("Logger/QueuedCycles", receiverQueue.size_approx());

		receiverQueueFault = !receiverQueue.try_enqueue(entry);
		if (receiverQueueFault)
			FRC_ReportError(frc::err::Error,
					"[AdvantageKit] Capacity of receiver queue exceeded, data will NOT be logged");
	}
}

units::second_t Logger::GetTimestamp() {
	if (!running)
		return frc::Timer::GetFPGATimestamp();
	std::lock_guard lock { entryMutex };
	return entry.GetTimestamp();
}

void Logger::RunEveryN(size_t n, std::function<void()> function) {
	if (cycleCount % n == 0)
		function();
}

void Logger::ProcessInputs(std::string key, inputs::LoggableInputs &inputs) {
	if (running) {
		if (!replaySource)
			inputs.ToLog(entry.GetSubtable(key));
		else
			inputs.FromLog(entry.GetSubtable(key));
	}
}
