// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once
#include <string_view>
#include "akit/LogTable.h"

namespace akit {

class LogDataReceiver {
public:
	static constexpr std::string_view TIMESTAMP_KEY = "/Timestamp";

	virtual void Start() = 0;
	virtual void End() = 0;
	virtual void PutTable(LogTable &table) = 0;
};

}
