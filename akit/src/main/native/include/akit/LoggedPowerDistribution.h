// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once
#include <hal/PowerDistribution.h>
#include "akit/LogTable.h"

namespace akit {

class LoggedPowerDistribution {
public:
	static LoggedPowerDistribution& getInstance();

	static LoggedPowerDistribution& getInstance(int moduleID,
			HAL_PowerDistributionType moduleType);

	void saveToLog(LogTable &&table);

	LoggedPowerDistribution() : LoggedPowerDistribution {
			HAL_DEFAULT_POWER_DISTRIBUTION_MODULE,
			HAL_PowerDistributionType_kAutomatic } {
	}

	LoggedPowerDistribution(int moduleID, HAL_PowerDistributionType moduleType);

private:
	static std::unique_ptr<LoggedPowerDistribution> instance;
	int moduleID;
	HAL_PowerDistributionType moduleType;
};

}
