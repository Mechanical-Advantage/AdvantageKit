#include "akit/LoggedRobot.h"
#include <frc/Timer.h>

using namespace akit;

LoggedRobot::LoggedRobot(units::second_t period) : frc::IterativeRobotBase {period} {
    HAL_SetNotifierName(notifier, "LoggedRobot", nullptr);
    HAL_Report(HALUsageReporting::kResourceType_Framework, HALUsageReporting::kFramework_AdvantageKit);
    HAL_Report(HALUsageReporting::kResourceType_LoggingFramework, HALUsageReporting::kLoggingFramework_AdvantageKit);
}

LoggedRobot::~LoggedRobot() {
    HAL_StopNotifier(notifier, nullptr);
    HAL_CleanNotifier(notifier);
}

void LoggedRobot::StartCompetition() {
    RobotInit();
    if (IsSimulation())
        SimulationInit();

    std::puts("\n********** Robot program startup complete **********");
    HAL_ObserveUserProgramStarting();

    while (true) {
        if (useTiming) {
            units::second_t currentTimeUs = frc::Timer::GetFPGATimestamp();
            if (nextCycleUs < currentTimeUs)
                nextCycleUs = currentTimeUs;
            else {
                HAL_UpdateNotifierAlarm(notifier, units::microsecond_t{nextCycleUs}.value(), nullptr);
                if (HAL_WaitForNotifierAlarm(notifier, nullptr) == 0) {
                    // Logger.end();
                    // break;
                }
            }
            nextCycleUs += periodUs;
        }

        LoopFunc();
    }
}

void LoggedRobot::EndCompetition() {
    HAL_StopNotifier(notifier ,nullptr);
}
