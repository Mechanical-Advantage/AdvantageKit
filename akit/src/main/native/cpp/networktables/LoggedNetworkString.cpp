// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include "akit/networktables/LoggedNetworkString.h"
#include "akit/Logger.h"

using namespace akit::nt;

LoggedNetworkString::LoggedNetworkString(std::string key) : key { key }, entry {
		::nt::NetworkTableInstance::GetDefault().GetStringTopic(key).GetEntry(
				"") } {
	Logger::RegisterDashboardInput(*this);
}

LoggedNetworkString::LoggedNetworkString(std::string key,
		std::string defaultValue) : LoggedNetworkString { key } {
	SetDefault(defaultValue);
	value = defaultValue;
}

void LoggedNetworkString::SetDefault(std::string defaultValue) {
	this->defaultValue = defaultValue;
	entry.Set(entry.Get(defaultValue));
}

void LoggedNetworkString::Set(std::string value) {
	entry.Set(value);
}

std::string LoggedNetworkString::Get() {
	return value;
}

void LoggedNetworkString::ToLog(LogTable &&table) {
	table.Put(RemoveSlash(key), value);
}

void LoggedNetworkString::FromLog(LogTable &&table) {
	value = table.Get(RemoveSlash(key), defaultValue);
}

void LoggedNetworkString::Periodic() {
	if (!Logger::HasReplaySource())
		value = entry.Get(defaultValue);
	Logger::ProcessInputs(std::string { PREFIX }, *this);
}
