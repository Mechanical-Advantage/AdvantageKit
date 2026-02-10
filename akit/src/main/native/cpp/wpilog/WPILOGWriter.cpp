// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include <random>
#include <iomanip>
#include <iostream>
#include <fstream>
#include <filesystem>
#include <frc/DriverStation.h>
#include <frc/RobotController.h>
#include "akit/wpilog/WPILOGWriter.h"
#include "akit/wpilog/WPILOGConstants.h"

using namespace akit::wpilog;
namespace fs = std::filesystem;

WPILOGWriter::WPILOGWriter(std::string path,
		AdvantageScopeOpenBehavior openBehavior) : openBehavior { openBehavior } {
	std::mt19937 gen { std::random_device { }() };
	std::uniform_int_distribution dis { 0, 0xFFFF };
	std::stringstream ss;
	for (int i = 0; i < 4; i++)
		ss << std::hex << std::setw(4) << std::setfill('0') << dis(gen);
	randomIdentifier = ss.str();

	fs::path fsPath { path };
	if (fsPath.extension() == ".wpilog") {
		folder = fsPath.parent_path().string();
		filename = fsPath.filename().string();
		autoRename = false;
	} else {
		folder = path;
		filename = "akit_" + randomIdentifier + ".wpilog";
		autoRename = true;
	}
}

void WPILOGWriter::start() {
	fs::path logFolder { folder };
	if (!fs::exists(logFolder))
		fs::create_directories(logFolder);

	fs::path logFile { logFolder / filename };
	if (fs::exists(logFile))
		fs::remove(logFile);

	std::cout << "[AdvantageKit] Logging to \"" << logFile.string() << "\"\n";

	std::error_code code;
	log = std::make_unique < wpi::log::DataLogWriter
			> (logFile.string(), code, WPILOGConstants::EXTRA_HEADER);

	isOpen = true;
	timestampID =
			log->Start(TIMESTAMP_KEY,
					LogTable::WPILOG_TYPES[static_cast<int>(LogTable::LoggableType::Integer)]);
	lastTable = LogTable { 0 };
}

void WPILOGWriter::end() {
	log.release();

	bool shouldOpen;
	switch (openBehavior) {
	case AdvantageScopeOpenBehavior::ALWAYS:
		shouldOpen = frc::RobotBase::IsSimulation();
		break;
	case AdvantageScopeOpenBehavior::AUTO:
		shouldOpen = frc::RobotBase::IsSimulation(); // && Logger.hasReplaySource();
		break;
	case AdvantageScopeOpenBehavior::NEVER:
		shouldOpen = false;
		break;
	}
	if (shouldOpen) {
		std::string fullLogPath =
				fs::absolute(fs::path { folder } / filename).string();
		fs::path advantageScopeTempPath = fs::temp_directory_path()
				/ ADVANTAGESCOPE_FILE_NAME;
		std::ofstream writer { advantageScopeTempPath };
		writer << fullLogPath;
		std::cout << "[AdvantageKit] Log sent to AdvantageScope.\n";
	}
}

void WPILOGWriter::putTable(LogTable &&table) {
	if (!isOpen)
		return;

	if (autoRename) {
		// Update timestamp
		if (!logDate) {
			if ((table.get("DriverStation/DSAttached", false)
					&& table.get("SystemStats/SystemTimeValid", false))
					|| frc::RobotBase::IsSimulation()) {
				if (!dsAttachedTime)
					dsAttachedTime = frc::RobotController::GetFPGATime()
							/ 1000000;
				else if (frc::RobotController::GetFPGATime() / 1000000
						- *dsAttachedTime > TIMESTAMP_UPDATE_DELAY
						|| frc::RobotBase::IsSimulation())
					logDate = std::chrono::system_clock::now();
			} else
				dsAttachedTime.reset();
		}

		frc::DriverStation::MatchType matchType = frc::DriverStation::kNone;
		switch (table.get("DriverStation/MatchType", 0L)) {
		case 1:
			matchType = frc::DriverStation::kPractice;
			break;
		case 2:
			matchType = frc::DriverStation::kQualification;
			break;
		case 3:
			matchType = frc::DriverStation::kElimination;
			break;
		}
		if (logMatchText.empty() && matchType != frc::DriverStation::kNone) {
			logMatchText = "";
			switch (matchType) {
			case frc::DriverStation::kPractice:
				logMatchText = "p";
				break;
			case frc::DriverStation::kQualification:
				logMatchText = "q";
				break;
			case frc::DriverStation::kElimination:
				logMatchText = "e";
				break;
			default:
				break;
			}
			logMatchText += std::to_string(
					table.get("DriverStation/MatchNumber", 0L));
		}

		std::string eventName = table.get("DriverStation/EventName",
				std::string { "" });
		std::transform(eventName.begin(), eventName.end(), eventName.begin(),
				::tolower);

		std::string newFilename = "akit_";
	}
}
