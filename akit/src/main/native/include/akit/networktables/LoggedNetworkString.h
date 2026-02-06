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
	LoggedNetworkString(std::string key) : key { key }, entry {
			::nt::NetworkTableInstance::GetDefault().GetStringTopic(key).GetEntry(
					"") } {
		// Logger.registerDashboardInput(this)
	}

	LoggedNetworkString(std::string key, std::string defaultValue) : LoggedNetworkString {
			key } {
		setDefault(defaultValue);
		value = defaultValue;
	}

	void setDefault(std::string defaultValue) {
		this->defaultValue = defaultValue;
		entry.Set(entry.Get(defaultValue));
	}

	void set(std::string value) {
		entry.Set(value);
	}

	std::string get() {
		return value;
	}

	void toLog(LogTable table) override {
		table.put(removeSlash(key), value);
	}

	void fromLog(LogTable table) override {
		value = table.get(removeSlash(key), defaultValue);
	}

	void periodic() {
		// if (!Logger.hasReplaySource()) {
		//   value = entry.get(defaultValue);
		// }
		// Logger.processInputs(prefix, inputs);
	}

private:
	std::string key;
	::nt::StringEntry entry;
	std::string defaultValue;
	std::string value;
};

}

}
