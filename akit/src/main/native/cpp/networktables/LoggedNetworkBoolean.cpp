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
	Logger::RegisterDashboardInput(*this);
}

LoggedNetworkBoolean::LoggedNetworkBoolean(std::string key, bool defaultValue) : LoggedNetworkBoolean {
		key } {
	SetDefault(defaultValue);
	value = defaultValue;
}

void LoggedNetworkBoolean::SetDefault(bool defaultValue) {
	this->defaultValue = defaultValue;
	entry.Set(entry.Get(defaultValue));
}

void LoggedNetworkBoolean::Set(bool value) {
	entry.Set(value);
}

bool LoggedNetworkBoolean::Get() {
	return value;
}

void LoggedNetworkBoolean::ToLog(LogTable &&table) {
	table.Put(RemoveSlash(key), value);
}

void LoggedNetworkBoolean::FromLog(LogTable &&table) {
	value = table.Get(RemoveSlash(key), defaultValue);
}

void LoggedNetworkBoolean::Periodic() {
	if (!Logger::HasReplaySource())
		value = entry.Get(defaultValue);
	Logger::ProcessInputs(std::string { PREFIX }, *this);
}
