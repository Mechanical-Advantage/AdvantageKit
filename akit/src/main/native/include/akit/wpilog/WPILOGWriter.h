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
		ALWAYS, AUTO, NEVER,
	};

	WPILOGWriter(std::string_view path = DEFAULT_PATH,
			AdvantageScopeOpenBehavior openBehavior =
					AdvantageScopeOpenBehavior::AUTO);

	void Start() override;

	void End() override;

	void PutTable(LogTable &table) override;

private:
	static constexpr units::second_t TIMESTAMP_UPDATE_DELAY = 5_s;
	static constexpr std::string_view DEFAULT_PATH =
			frc::RobotBase::IsSimulation() ? "logs" : "/U/logs";
	static constexpr std::string_view TIME_FORMATTER = "%y-%m-%d_%H-%M-%S";
	static constexpr std::string_view ADVANTAGESCOPE_FILENAME =
			"ascope-log-path.txt";

	static std::string GetUnitMetadata(std::string_view unit);

	bool isOpen = false;
	bool autoRename;
	int timestampID;
	LogTable lastTable { 0_s };
	AdvantageScopeOpenBehavior openBehavior;
	std::string logMatchText;
	std::string randomIdentifier;
	std::filesystem::path folder;
	std::filesystem::path filename;
	std::unique_ptr<wpi::log::DataLogWriter> log;
	std::unordered_map<std::string, int> entryIDs;
	std::unordered_map<std::string, LogTable::LoggableType> entryTypes;
	std::unordered_map<std::string, std::string> entryUnits;
	std::optional<std::tm> logDate;
	std::optional<units::millisecond_t> dsAttachedTime;
};

}

}
