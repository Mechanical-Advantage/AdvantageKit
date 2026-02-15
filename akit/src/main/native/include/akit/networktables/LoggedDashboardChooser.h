// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once
#include <frc/smartdashboard/SendableChooser.h>
#include <frc/smartdashboard/SmartDashboard.h>
#include "akit/networktables/LoggedNetworkInput.h"
#include "akit/inputs/LoggableInputs.h"
#include "akit/LogTable.h"
#include "akit/Logger.h"

namespace akit {

namespace nt {

template<typename T>
class LoggedDashboardChooser: public LoggedNetworkInput,
		public inputs::LoggableInputs {
public:
	void ToLog(LogTable table) {
		table.Put(key, selectedValue);
	}

	void FromLog(LogTable table) {
		selectedValue = table.Get(key, selectedValue);
	}

	LoggedDashboardChooser(std::string key) : key { key } {
		frc::SmartDashboard::PutData(key, &sendableChooser);
		Periodic();
		Logger::RegisterDashboardInput(*this);
	}

	void AddOption(std::string key, T value) {
		sendableChooser.AddOption(key, key);
		options.emplace(key, value);
	}

	void AddDefaultOption(std::string key, T value) {
		sendableChooser.SetDefaultOption(key, key)
		options.emplace(key, value);
	}

	T Get() {
		return options.at(selectedValue);
	}

	void OnChange(std::function<T()> listener) {
		this->listener = listener;
	}

	frc::SendableChooser<std::string> GetSendableChooser() {
		return sendableChooser;
	}

	void Periodic() override {
		if (!Logger::HasReplaySource())
			selectedValue = sendableChooser.GetSelected();
		Logger::ProcessInputs(prefix + "/SmartDashboard", *this);
		if (previousValue != selectedValue) {
			if (listener)
				listener(Get());
			previousValue = selectedValue;
		}
	}

private:
	std::string key;
	std::string selectedValue;
	std::string previousValue;
	frc::SendableChooser<std::string> sendableChooser;
	std::unordered_map<std::string, T> options;
	std::function<void(T)> listener;
};

}

}
