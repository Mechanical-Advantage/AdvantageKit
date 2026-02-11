// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include "akit/LoggedPowerDistribution.h"
#include "akit/conduit/ConduitApi.h"

using namespace akit;

std::unique_ptr<LoggedPowerDistribution> LoggedPowerDistribution::instance;

LoggedPowerDistribution& LoggedPowerDistribution::getInstance() {
	if (!instance)
		instance = std::make_unique<LoggedPowerDistribution>();
	return *instance;
}

LoggedPowerDistribution& LoggedPowerDistribution::getInstance(int moduleID,
		HAL_PowerDistributionType moduleType) {
	if (!instance)
		instance = std::make_unique < LoggedPowerDistribution
				> (moduleID, moduleType);
	return *instance;
}

void LoggedPowerDistribution::saveToLog(LogTable &&table) {
	conduit::ConduitApi &inst = conduit::ConduitApi::getInstance();
	table.put("Temperature", inst.getPDPTemperature());
	table.put("Voltage", inst.getPDPVoltage());
	std::array<double, 24> currents;
	for (size_t i = 0; i < currents.size(); i++)
		currents[i] = inst.getPDPChannelCurrent(i);
	table.put("ChannelCurrent",
			std::vector<double> { currents.begin(), currents.end() });
	table.put("TotalCurrent", inst.getPDPTotalCurrent());
	table.put("TotalPower", inst.getPDPTotalPower());
	table.put("TotalEnergy", inst.getPDPTotalEnergy());

	table.put("ChannelCount", inst.getPDPChannelCount());
	table.put("Faults", inst.getPDPFaults());
	table.put("StickyFaults", inst.getPDPStickyFaults());
}

LoggedPowerDistribution::LoggedPowerDistribution(int moduleID,
		HAL_PowerDistributionType moduleType) : moduleID { moduleID }, moduleType {
		moduleType } {
	conduit::ConduitApi::getInstance().configurePowerDistribution(moduleID,
			moduleType);
}
