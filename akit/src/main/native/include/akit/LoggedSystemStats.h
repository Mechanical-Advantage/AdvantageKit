// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once
#include <unordered_set>
#include <string>
#include <span>
#include <networktables/NetworkTableInstance.h>
#include "akit/LogTable.h"

namespace akit {

class LoggedSystemStats {
public:
	void saveToLog(LogTable &&table);

private:
	std::unordered_set<std::string> lastNTRemoteIds;
	std::array<std::byte, 4> ntIntBuffer;
};

}
