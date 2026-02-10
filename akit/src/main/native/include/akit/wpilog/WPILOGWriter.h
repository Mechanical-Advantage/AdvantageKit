// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once
#include <wpi/DataLogWriter.h>
#include <frc/RobotBase.h>
#include "akit/LogDataReceiver.h"

namespace akit {

namespace wpilog {

class WPILOGWriter: public LogDataReceiver {
public:
	enum class AdvantageScopeOpenBehavior {
		ALWAYS, AUTO, NEVER
	};

	WPILOGWriter(std::string path, AdvantageScopeOpenBehavior openBehavior);

	WPILOGWriter(std::string path) : WPILOGWriter { path,
			AdvantageScopeOpenBehavior::AUTO } {
	}

	WPILOGWriter(AdvantageScopeOpenBehavior openBehavior) : WPILOGWriter {
			std::string {
					frc::RobotBase::IsSimulation() ?
							DEFAULT_PATH_SIM : DEFAULT_PATH_RIO }, openBehavior } {
	}

	WPILOGWriter() : WPILOGWriter {
			std::string {
					frc::RobotBase::IsSimulation() ?
							DEFAULT_PATH_SIM : DEFAULT_PATH_RIO },
			AdvantageScopeOpenBehavior::AUTO } {
	}

	void start() override;

	void end() override;

	void putTable(LogTable &&table) override;

private:
	static constexpr double TIMESTAMP_UPDATE_DELAY = 5;
	static constexpr std::string_view DEFAULT_PATH_RIO = "/U/logs";
	static constexpr std::string_view DEFAULT_PATH_SIM = "logs";
	static constexpr std::string_view ADVANTAGESCOPE_FILE_NAME =
			"ascope-log-path.txt";

	std::string folder;
	std::string filename;
	std::string randomIdentifier;
	std::optional<double> dsAttachedTime;

	bool autoRename;
	std::optional<std::chrono::system_clock::time_point> logDate;
	std::string logMatchText;

	std::unique_ptr<wpi::log::DataLogWriter> log;
	bool isOpen = false;
	AdvantageScopeOpenBehavior openBehavior;
	std::optional<LogTable> lastTable;
	int timestampID;
	std::unordered_map<std::string, int> entryIDs;
	std::unordered_map<std::string, LogTable::LoggableType> entryTypes;
	std::unordered_map<std::string, std::string> entryUnits;
};

}

}
