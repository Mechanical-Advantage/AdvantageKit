// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include <frc/Timer.h>
#include "akit/LoggedRobot.h"
#include "akit/Logger.h"

using namespace akit;

LoggedRobot::LoggedRobot(units::second_t period) : frc::IterativeRobotBase {
		period } {
	HAL_SetNotifierName(notifier, "LoggedRobot", nullptr);
	HAL_Report(HALUsageReporting::kResourceType_Framework,
			HALUsageReporting::kFramework_AdvantageKit);
	HAL_Report(HALUsageReporting::kResourceType_LoggingFramework,
			HALUsageReporting::kLoggingFramework_AdvantageKit);
}

LoggedRobot::~LoggedRobot() {
	HAL_StopNotifier(notifier, nullptr);
	HAL_CleanNotifier (notifier);
}

void LoggedRobot::StartCompetition() {
	RobotInit();
	if (IsSimulation())
		SimulationInit();

	units::millisecond_t initEnd = frc::Timer::GetFPGATimestamp();

	Logger::periodicAfterUser(initEnd, 0_s);

	std::puts("\n********** Robot program startup complete **********");
	HAL_ObserveUserProgramStarting();

	while (true) {
		if (useTiming) {
			units::second_t currentTimeUs = frc::Timer::GetFPGATimestamp();
			if (nextCycleUs < currentTimeUs)
				nextCycleUs = currentTimeUs;
			else {
				HAL_UpdateNotifierAlarm(notifier, units::microsecond_t {
						nextCycleUs }.value(), nullptr);
				if (HAL_WaitForNotifierAlarm(notifier, nullptr) == 0) {
					Logger::end();
					break;
				}
			}
			nextCycleUs += periodUs;
		}

		units::millisecond_t periodicBeforeStart =
				frc::Timer::GetFPGATimestamp();
		Logger::periodicBeforeUser();
		units::millisecond_t userCodeStart = frc::Timer::GetFPGATimestamp();
		LoopFunc();
		units::second_t userCodeEnd = frc::Timer::GetFPGATimestamp();

		Logger::periodicAfterUser(userCodeEnd - userCodeStart,
				userCodeStart - periodicBeforeStart);
	}
}

void LoggedRobot::EndCompetition() {
	HAL_StopNotifier(notifier, nullptr);
}
