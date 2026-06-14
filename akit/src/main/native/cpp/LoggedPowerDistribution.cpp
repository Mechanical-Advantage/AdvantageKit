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

LoggedPowerDistribution& LoggedPowerDistribution::GetInstance() {
	if (!instance)
		instance = std::make_unique<LoggedPowerDistribution>();
	return *instance;
}

LoggedPowerDistribution& LoggedPowerDistribution::GetInstance(int moduleID,
		HAL_PowerDistributionType moduleType) {
	if (!instance)
		instance = std::make_unique < LoggedPowerDistribution
				> (moduleID, moduleType);
	return *instance;
}

void LoggedPowerDistribution::SaveToLog(LogTable &&table) {
	conduit::ConduitApi &inst = conduit::ConduitApi::getInstance();
	table.Put("Temperature", inst.getPDPTemperature());
	table.Put("Voltage", inst.getPDPVoltage());
	std::array<double, 24> currents;
	for (size_t i = 0; i < currents.size(); i++)
		currents[i] = inst.getPDPChannelCurrent(i);
	table.Put("ChannelCurrent",
			std::vector<double> { currents.begin(), currents.end() });
	table.Put("TotalCurrent", inst.getPDPTotalCurrent());
	table.Put("TotalPower", inst.getPDPTotalPower());
	table.Put("TotalEnergy", inst.getPDPTotalEnergy());

	table.Put("ChannelCount", inst.getPDPChannelCount());
	table.Put("Faults", inst.getPDPFaults());
	table.Put("StickyFaults", inst.getPDPStickyFaults());
}

LoggedPowerDistribution::LoggedPowerDistribution(int moduleID,
		HAL_PowerDistributionType moduleType) : moduleID { moduleID }, moduleType {
		moduleType } {
	conduit::ConduitApi::getInstance().configurePowerDistribution(moduleID,
			moduleType);
}
