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
	LoggedNetworkBoolean(std::string key) : key { key }, entry {
			::nt::NetworkTableInstance::GetDefault().GetBooleanTopic(key).GetEntry(
					false) }, value { defaultValue } {
		// Logger.registerDashboardInput(this)
	}

	LoggedNetworkBoolean(std::string key, bool defaultValue) : LoggedNetworkBoolean {
			key } {
		setDefault(defaultValue);
		value = defaultValue;
	}

	void setDefault(bool defaultValue) {
		this->defaultValue = defaultValue;
		entry.Set(entry.Get(defaultValue));
	}

	void set(bool value) {
		entry.Set(value);
	}

	bool get() {
		return value;
	}

	void toLog(LogTable table) override {
		table.put(removeSlash(key), value);
	}

	void fromLog(LogTable table) override {
		value = table.get(removeSlash(key), defaultValue);
	}

	void periodic() {
		/*
		 if (!Logger.hasReplaySource()) {
		 value = entry.get(defaultValue);
		 }
		 Logger.processInputs(prefix, inputs);
		 */
	}

private:
	std::string key;
	::nt::BooleanEntry entry;
	bool defaultValue;
	bool value;
};

}

}
