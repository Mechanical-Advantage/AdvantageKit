// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once
#include "akit/LogTable.h"

namespace akit {

class LogReplaySource {
public:
	virtual void start() = 0;

	virtual void end() = 0;

	virtual bool updateTable(LogTable&& table);
};

}
