// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once
#include <frc/IterativeRobotBase.h>
#include <hal/FRCUsageReporting.h>
#include <frc/RobotController.h>
#include <hal/Notifier.h>

namespace akit {

class LoggedRobot: frc::IterativeRobotBase {
public:
	// Consistency with TimedRobot::kDefaultPeriod
	static constexpr units::second_t kDefaultPeriod = 20_ms;

	void StartCompetition() override;

	void EndCompetition() override;

protected:
	LoggedRobot() : LoggedRobot { kDefaultPeriod } {
	}

	LoggedRobot(units::second_t period);

	~LoggedRobot() override;

	void SetUseTiming(bool useTiming) {
		this->useTiming = useTiming;
	}

private:
	// So that Logger can access isLoggedRobot but user cannot modify
	friend class Logger;
	static bool isLoggedRobot;

	HAL_NotifierHandle notifier { HAL_InitializeNotifier(nullptr) };
	bool useTiming = true;
	units::second_t nextCycleUs;
	units::second_t periodUs;
};

}
