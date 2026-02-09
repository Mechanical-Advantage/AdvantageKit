// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once
#include <networktables/StringTopic.h>
#include <networktables/NetworkTableInstance.h>
#include "akit/networktables/LoggedNetworkInput.h"
#include "akit/inputs/LoggableInputs.h"
#include "akit/LogTable.h"

namespace akit {

namespace nt {

class LoggedNetworkString: public LoggedNetworkInput,
		public inputs::LoggableInputs {
public:
	LoggedNetworkString(std::string key);

	LoggedNetworkString(std::string key, std::string defaultValue);

	void setDefault(std::string defaultValue);

	void set(std::string value);

	std::string get();

	void toLog(LogTable &&table) override;

	void fromLog(LogTable &&table) override;

	void periodic() override;

private:
	std::string key;
	::nt::StringEntry entry;
	std::string defaultValue;
	std::string value;
};

}

}
