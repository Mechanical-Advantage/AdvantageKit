// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once
#include <wpi/DataLogReader.h>
#include "akit/LogReplaySource.h"

namespace akit {

namespace wpilog {

class WPILOGReader: LogReplaySource {
public:
	WPILOGReader(std::string filename) : filename { filename } {
	}

	void start() override;
	bool updateTable(LogTable &table);

private:
	std::string filename;
	bool isValid;

	std::optional<wpi::log::DataLogReader> reader;
	std::optional<wpi::log::DataLogIterator> iterator;

	std::optional<units::second_t> timestamp;
	std::unordered_map<int, std::string> entryIDs;
	std::unordered_map<int, LogTable::LoggableType> entryTypes;
	std::unordered_map<int, std::string> entryCustomTypes;
};

}

}
