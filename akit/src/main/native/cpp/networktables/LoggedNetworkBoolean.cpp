// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include "akit/networktables/LoggedNetworkBoolean.h"
#include "akit/Logger.h"

using namespace akit::nt;

LoggedNetworkBoolean::LoggedNetworkBoolean(std::string key) : key { key }, entry {
		::nt::NetworkTableInstance::GetDefault().GetBooleanTopic(key).GetEntry(
				false) }, value { defaultValue } {
	Logger::registerDashboardInput(*this);
}

LoggedNetworkBoolean::LoggedNetworkBoolean(std::string key, bool defaultValue) : LoggedNetworkBoolean {
		key } {
	setDefault(defaultValue);
	value = defaultValue;
}

void LoggedNetworkBoolean::setDefault(bool defaultValue) {
	this->defaultValue = defaultValue;
	entry.Set(entry.Get(defaultValue));
}

void LoggedNetworkBoolean::set(bool value) {
	entry.Set(value);
}

bool LoggedNetworkBoolean::get() {
	return value;
}

void LoggedNetworkBoolean::toLog(LogTable &&table) {
	table.put(removeSlash(key), value);
}

void LoggedNetworkBoolean::fromLog(LogTable &&table) {
	value = table.get(removeSlash(key), defaultValue);
}

void LoggedNetworkBoolean::periodic() {
	if (!Logger::hasReplaySource())
		value = entry.Get(defaultValue);
	Logger::processInputs(std::string { PREFIX }, *this);
}
