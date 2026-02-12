// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once
#include "akit/LogTable.h"

namespace akit {

class Logger {
private:
	static constexpr int RECEIVER_QUEUE_CAPACITY = 500;

	static bool running;
	static long cycleCount;
	static LogTable entry;
	static LogTable outputTable;
	static std::unordered_map<std::string, std::string> metadata;
};

}
