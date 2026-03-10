// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once
#include <networktables/BooleanTopic.h>
#include <networktables/NetworkTableInstance.h>
#include "akit/networktables/LoggedNetworkInput.h"
#include "akit/inputs/LoggableInputs.h"
#include "akit/LogTable.h"

namespace akit {

namespace nt {

class LoggedNetworkBoolean: public LoggedNetworkInput,
		public inputs::LoggableInputs {
public:
	LoggedNetworkBoolean(std::string key);

	LoggedNetworkBoolean(std::string key, bool defaultValue);

	void SetDefault(bool defaultValue);

	void Set(bool value);

	bool Get();

	void ToLog(LogTable &&table) override;

	void FromLog(LogTable &&table) override;

	void Periodic() override;

private:
	std::string key;
	::nt::BooleanEntry entry;
	bool defaultValue;
	bool value;
};

}

}
