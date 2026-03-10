// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include <frc/DriverStation.h>
#include <frc/simulation/DriverStationSim.h>

#include "akit/LoggedDriverStation.h"
#include "akit/conduit/ConduitApi.h"

using namespace akit;

void LoggedDriverStation::SaveToLog(LogTable &&table) {
	conduit::ConduitApi inst = conduit::ConduitApi::getInstance();

	table.Put("AllianceStation", inst.getAllianceStation());
	// TODO: Trim event name + game specific message
	table.Put("EventName", inst.getEventName());
	table.Put("GameSpecificMessage", inst.getGameSpecificMessage());
	table.Put("MatchNumber", inst.getMatchNumber());
	table.Put("ReplayNumber", inst.getReplayNumber());
	table.Put("MatchType", inst.getMatchType());
	table.Put("MatchTime", inst.getMatchTime());

	int32_t controlWord = inst.getControlWord();
	table.Put("Enabled", (controlWord & 1) != 0);
	table.Put("Autonomous", (controlWord & 2) != 0);
	table.Put("Test", (controlWord & 4) != 0);
	table.Put("EmergencyStop", (controlWord & 8) != 0);
	table.Put("FMSAttached", (controlWord & 16) != 0);
	table.Put("DSAttached", (controlWord & 32) != 0);

	for (int id = 0; id < frc::DriverStation::kJoystickPorts; id++) {
		LogTable joystickTable = table.GetSubtable(
				"Joystick" + std::to_string(id));
		// TODO: Trim joystick name
		joystickTable.Put("Name", inst.getJoystickName(id));
		joystickTable.Put("Type", inst.getJoystickType(id));
		joystickTable.Put("Xbox", inst.isXbox(id));
		joystickTable.Put("ButtonCount", inst.getButtonCount(id));
		joystickTable.Put("ButtonValues", inst.getButtonValues(id));

		int16_t povCount = inst.getPovCount(id);
		auto rawPovValues = inst.getPovValues(id);
		std::vector<long> povValues { rawPovValues.begin(), rawPovValues.begin()
				+ povCount };
		joystickTable.Put("POVs", povValues);

		int16_t axisCount = inst.getAxisCount(id);
		auto rawAxisValues = inst.getAxisValues(id);
		auto rawAxisTypes = inst.getAxisTypes(id);
		std::vector<float> axisValues { rawAxisValues.begin(),
				rawAxisValues.begin() + axisCount };
		std::vector<long> axisTypes { rawAxisTypes.begin(), rawAxisTypes.begin()
				+ axisCount };
		joystickTable.Put("AxisValues", axisValues);
		joystickTable.Put("AxisTypes", axisTypes);
	}
}

void LoggedDriverStation::ReplayFromLog(LogTable &&table) {
	frc::sim::DriverStationSim::SetAllianceStationId(
			static_cast<HAL_AllianceStationID>(table.Get("AllianceStation", 0)));
	frc::sim::DriverStationSim::SetEventName(
			table.Get("EventName", std::string { "" }));
	frc::sim::DriverStationSim::SetGameSpecificMessage(
			table.Get("GameSpecificMessage", std::string { "" }));
	frc::sim::DriverStationSim::SetMatchNumber(table.Get("MatchNumber", 0));
	frc::sim::DriverStationSim::SetReplayNumber(table.Get("ReplayNumber", 0));

	frc::DriverStation::MatchType matchType = frc::DriverStation::kNone;
	switch (table.Get("MatchType", 0)) {
	case 1:
		matchType = frc::DriverStation::kPractice;
		break;
	case 2:
		matchType = frc::DriverStation::kQualification;
		break;
	case 3:
		matchType = frc::DriverStation::kElimination;
		break;
	}
	frc::sim::DriverStationSim::SetMatchType(matchType);

	bool dsAttached = table.Get("DSAttached", false);
	frc::sim::DriverStationSim::SetEnabled(table.Get("Enabled", false));
	frc::sim::DriverStationSim::SetAutonomous(table.Get("Autonomous", false));
	frc::sim::DriverStationSim::SetTest(table.Get("Test", false));
	frc::sim::DriverStationSim::SetEStop(table.Get("EmergencyStop", false));
	frc::sim::DriverStationSim::SetFmsAttached(table.Get("FMSAttached", false));
	frc::sim::DriverStationSim::SetDsAttached(dsAttached);

	for (int id = 0; id < frc::DriverStation::kJoystickPorts; id++) {
		LogTable joystickTable = table.GetSubtable(
				"Joystick" + std::to_string(id));
		frc::sim::DriverStationSim::SetJoystickName(id,
				joystickTable.Get("Name", std::string { "" }));
		frc::sim::DriverStationSim::SetJoystickType(id,
				joystickTable.Get("Type", 0));
		frc::sim::DriverStationSim::SetJoystickIsXbox(id,
				joystickTable.Get("Xbox", false));
		frc::sim::DriverStationSim::SetJoystickButtonCount(id,
				joystickTable.Get("ButtonCount", 0));
		frc::sim::DriverStationSim::SetJoystickButtons(id,
				joystickTable.Get("ButtonValues", 0));

		std::vector<long> povValues = joystickTable.Get("POVs",
				std::vector<long> { });
		frc::sim::DriverStationSim::SetJoystickPOVCount(id, povValues.size());
		for (size_t i = 0; i < povValues.size(); i++)
			frc::sim::DriverStationSim::SetJoystickPOV(id, i, povValues[i]);

		std::vector<float> axisValues = joystickTable.Get("AxisValues",
				std::vector<float> { });
		std::vector<long> axisTypes = joystickTable.Get("AxisTypes",
				std::vector<long> { });
		frc::sim::DriverStationSim::SetJoystickAxisCount(id, axisValues.size());
		for (size_t i = 0; i < axisValues.size(); i++) {
			frc::sim::DriverStationSim::SetJoystickAxis(id, i, axisValues[i]);
			frc::sim::DriverStationSim::SetJoystickAxisType(id, i,
					axisTypes[i]);
		}

		if (dsAttached)
			frc::sim::DriverStationSim::NotifyNewData();
	}
}
