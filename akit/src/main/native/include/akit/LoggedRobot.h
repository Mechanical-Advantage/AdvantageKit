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

protected:
	LoggedRobot() : LoggedRobot { kDefaultPeriod } {
	}

	LoggedRobot(units::second_t period) : frc::IterativeRobotBase { period }, periodUs {
			period.value() * 1000000 } {
		HAL_SetNotifierName(notifier, "LoggedRobot", nullptr);
		HAL_Report(HALUsageReporting::kResourceType_Framework,
				HALUsageReporting::kFramework_AdvantageKit);
		HAL_Report(HALUsageReporting::kResourceType_LoggingFramework,
				HALUsageReporting::kLoggingFramework_AdvantageKit);
	}

	~LoggedRobot() override {
		HAL_StopNotifier(notifier, nullptr);
		HAL_CleanNotifier(notifier);
	}

	void StartCompetition() override {
		RobotInit();
		if (IsSimulation())
			SimulationInit();
		uint64_t initEnd = frc::RobotController::GetFPGATime();

		// AutoLogOutputManager.addObject(this);

		// Logger.periodicAfterUser(initEnd, 0);

		std::puts("\n********** Robot program startup complete **********");
		HAL_ObserveUserProgramStarting();

		while (true) {
			if (useTiming) {
				long currentTimeUs = frc::RobotController::GetFPGATime();
				if (nextCycleUs < currentTimeUs)
					nextCycleUs = currentTimeUs;
				else {
					HAL_UpdateNotifierAlarm(notifier, nextCycleUs, nullptr);
					if (HAL_WaitForNotifierAlarm(notifier, nullptr) == 0) {
						// Logger.end();
						// break;
					}
				}
				nextCycleUs += periodUs;
			}

			uint64_t periodicBeforeStart = frc::RobotController::GetFPGATime();
			// Logger.periodicBeforeUser();
			uint64_t userCodeStart = frc::RobotController::GetFPGATime();
			LoopFunc();
			uint64_t userCodeEnd = frc::RobotController::GetFPGATime();

			// Logger.periodicAfterUser(userCodeEnd - userCodeStart, userCodeStart - periodicBeforeStart);
		}
	}

	void EndCompetition() override {
		HAL_StopNotifier(notifier, nullptr);
	}

	void SetUseTiming(bool useTiming) {
		this->useTiming = useTiming;
	}

private:
	HAL_NotifierHandle notifier { HAL_InitializeNotifier(nullptr) };
	bool useTiming = true;
	uint64_t nextCycleUs;
	uint64_t periodUs;
};

}
