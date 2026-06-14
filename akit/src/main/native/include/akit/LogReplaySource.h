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
	virtual void Start() = 0;

	virtual ~LogReplaySource() = default;

	virtual bool UpdateTable(LogTable &table) = 0;
};

}
