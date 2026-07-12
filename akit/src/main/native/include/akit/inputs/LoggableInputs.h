// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once

namespace akit {

class LogTable;

namespace inputs {

class LoggableInputs {
public:
	virtual void ToLog(LogTable&&) = 0;
	virtual void FromLog(LogTable&&) = 0;
};

}

}
