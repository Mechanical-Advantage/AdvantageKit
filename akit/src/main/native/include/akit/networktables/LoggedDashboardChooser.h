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
	void toLog(LogTable table) {
		table.put(key, selectedValue);
	}

	void fromLog(LogTable table) {
		selectedValue = table.get(key, selectedValue);
	}

	LoggedDashboardChooser(std::string key) : key { key } {
		frc::SmartDashboard::PutData(key, &sendableChooser);
		periodic();
		Logger::registerDashboardInput(*this);
	}

	void addOption(std::string key, T value) {
		sendableChooser.AddOption(key, key);
		options.emplace(key, value);
	}

	void addDefaultOption(std::string key, T value) {
		sendableChooser.SetDefaultOption(key, key)
		options.emplace(key, value);
	}

	T get() {
		return options.at(selectedValue);
	}

	void onChange(std::function<T()> listener) {
		this->listener = listener;
	}

	frc::SendableChooser<std::string> getSendableChooser() {
		return sendableChooser;
	}

	void periodic() override {
		if (!Logger::hasReplaySource())
			selectedValue = sendableChooser.GetSelected();
		Logger::processInputs(prefix + "/SmartDashboard", *this);
		if (previousValue != selectedValue) {
			if (listener)
				listener(get());
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
