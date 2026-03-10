// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once
#include <networktables/DoubleTopic.h>
#include <networktables/NetworkTableInstance.h>
#include "akit/networktables/LoggedNetworkInput.h"
#include "akit/inputs/LoggableInputs.h"
#include "akit/LogTable.h"

namespace akit {

namespace nt {

class LoggedNetworkNumber: public LoggedNetworkInput,
		public inputs::LoggableInputs {
public:
	LoggedNetworkNumber(std::string key);

	LoggedNetworkNumber(std::string key, double defaultValue);

	void SetDefault(double defaultValue);

	void Set(double value);

	double Get();

	void ToLog(LogTable &&table);

	void FromLog(LogTable &&table);

	void Periodic() override;

private:
	std::string key;
	::nt::DoubleEntry entry;
	double defaultValue;
	double value;
};

}

}
