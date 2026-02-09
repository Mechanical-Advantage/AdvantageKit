// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include "akit/networktables/LoggedNetworkString.h"

using namespace akit::nt;

LoggedNetworkString::LoggedNetworkString(std::string key) : key { key }, entry {
		::nt::NetworkTableInstance::GetDefault().GetStringTopic(key).GetEntry(
				"") } {
}

LoggedNetworkString::LoggedNetworkString(std::string key,
		std::string defaultValue) : LoggedNetworkString { key } {
	setDefault(defaultValue);
	value = defaultValue;
}

void LoggedNetworkString::setDefault(std::string defaultValue) {
	this->defaultValue = defaultValue;
	entry.Set(entry.Get(defaultValue));
}

void LoggedNetworkString::set(std::string value) {
	entry.Set(value);
}

std::string LoggedNetworkString::get() {
	return value;
}

void LoggedNetworkString::toLog(LogTable &&table) {
	table.put(removeSlash(key), value);
}

void LoggedNetworkString::fromLog(LogTable &&table) {
	value = table.get(removeSlash(key), defaultValue);
}

void LoggedNetworkString::periodic() {
}
