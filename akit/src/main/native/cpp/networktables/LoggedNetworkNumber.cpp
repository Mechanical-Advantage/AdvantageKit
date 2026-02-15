// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include "akit/networktables/LoggedNetworkNumber.h"
#include "akit/Logger.h"

using namespace akit::nt;

LoggedNetworkNumber::LoggedNetworkNumber(std::string key) : key { key }, entry {
		::nt::NetworkTableInstance::GetDefault().GetDoubleTopic(key).GetEntry(0) }, value {
		defaultValue } {
	Logger::RegisterDashboardInput(*this);
}

LoggedNetworkNumber::LoggedNetworkNumber(std::string key, double defaultValue) : LoggedNetworkNumber {
		key } {
	SetDefault(defaultValue);
	value = defaultValue;
}

void LoggedNetworkNumber::SetDefault(double defaultValue) {
	this->defaultValue = defaultValue;
	entry.Set(entry.Get(defaultValue));
}

void LoggedNetworkNumber::Set(double value) {
	entry.Set(value);
}

double LoggedNetworkNumber::Get() {
	return entry.Get();
}

void LoggedNetworkNumber::ToLog(LogTable &&table) {
	table.Put(RemoveSlash(key), value);
}

void LoggedNetworkNumber::FromLog(LogTable &&table) {
	value = table.Get(RemoveSlash(key), defaultValue);
}

void LoggedNetworkNumber::Periodic() {
	if (!Logger::HasReplaySource())
		value = entry.Get(defaultValue);
	Logger::ProcessInputs(std::string { PREFIX }, *this);
}
