// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once
#include <units/time.h>
#include <frc/Notifier.h>
#include "akit/LogTable.h"

namespace akit {

class RadioLogger {
public:
	static void periodic(LogTable &&table);

private:
	static constexpr units::second_t REQUEST_PERIOD = 5_s;
	static constexpr units::millisecond_t TIMEOUT = 500_ms;

	static void start();

	static std::optional<frc::Notifier> notifier;
	static bool isConnected;
	static std::string statusJson;
};

}
