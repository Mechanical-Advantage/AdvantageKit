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

void LoggedDriverStation::saveToLog(LogTable &&table) {
	conduit::ConduitApi inst = conduit::ConduitApi::getInstance();

	table.put("AllianceStation", inst.getAllianceStation());
	// TODO: Trim event name + game specific message
	table.put("EventName", inst.getEventName());
	table.put("GameSpecificMessage", inst.getGameSpecificMessage());
	table.put("MatchNumber", inst.getMatchNumber());
	table.put("ReplayNumber", inst.getReplayNumber());
	table.put("MatchType", inst.getMatchType());
	table.put("MatchTime", inst.getMatchTime());

	int32_t controlWord = inst.getControlWord();
	table.put("Enabled", (controlWord & 1) != 0);
	table.put("Autonomous", (controlWord & 2) != 0);
	table.put("Test", (controlWord & 4) != 0);
	table.put("EmergencyStop", (controlWord & 8) != 0);
	table.put("FMSAttached", (controlWord & 16) != 0);
	table.put("DSAttached", (controlWord & 32) != 0);

	for (int id = 0; id < frc::DriverStation::kJoystickPorts; id++) {
		LogTable joystickTable = table.getSubtable(
				"Joystick" + std::to_string(id));
		// TODO: Trim joystick name
		joystickTable.put("Name", inst.getJoystickName(id));
		joystickTable.put("Type", inst.getJoystickType(id));
		joystickTable.put("Xbox", inst.isXbox(id));
		joystickTable.put("ButtonCount", inst.getButtonCount(id));
		joystickTable.put("ButtonValues", inst.getButtonValues(id));

		int16_t povCount = inst.getPovCount(id);
		auto rawPovValues = inst.getPovValues(id);
		std::vector<long> povValues { rawPovValues.begin(), rawPovValues.begin()
				+ povCount };
		joystickTable.put("POVs", povValues);

		int16_t axisCount = inst.getAxisCount(id);
		auto rawAxisValues = inst.getAxisValues(id);
		auto rawAxisTypes = inst.getAxisTypes(id);
		std::vector<float> axisValues { rawAxisValues.begin(),
				rawAxisValues.begin() + axisCount };
		std::vector<long> axisTypes { rawAxisTypes.begin(), rawAxisTypes.begin()
				+ axisCount };
		joystickTable.put("AxisValues", axisValues);
		joystickTable.put("AxisTypes", axisTypes);
	}
}

void LoggedDriverStation::replayFromLog(LogTable &&table) {
	frc::sim::DriverStationSim::SetAllianceStationId(
			static_cast<HAL_AllianceStationID>(table.get("AllianceStation", 0)));
	frc::sim::DriverStationSim::SetEventName(
			table.get("EventName", std::string { "" }));
	frc::sim::DriverStationSim::SetGameSpecificMessage(
			table.get("GameSpecificMessage", std::string { "" }));
	frc::sim::DriverStationSim::SetMatchNumber(table.get("MatchNumber", 0));
	frc::sim::DriverStationSim::SetReplayNumber(table.get("ReplayNumber", 0));

	frc::DriverStation::MatchType matchType = frc::DriverStation::kNone;
	switch (table.get("MatchType", 0)) {
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

	bool dsAttached = table.get("DSAttached", false);
	frc::sim::DriverStationSim::SetEnabled(table.get("Enabled", false));
	frc::sim::DriverStationSim::SetAutonomous(table.get("Autonomous", false));
	frc::sim::DriverStationSim::SetTest(table.get("Test", false));
	frc::sim::DriverStationSim::SetEStop(table.get("EmergencyStop", false));
	frc::sim::DriverStationSim::SetFmsAttached(table.get("FMSAttached", false));
	frc::sim::DriverStationSim::SetDsAttached(dsAttached);

	for (int id = 0; id < frc::DriverStation::kJoystickPorts; id++) {
		LogTable joystickTable = table.getSubtable(
				"Joystick" + std::to_string(id));
		frc::sim::DriverStationSim::SetJoystickName(id,
				joystickTable.get("Name", std::string { "" }));
		frc::sim::DriverStationSim::SetJoystickType(id,
				joystickTable.get("Type", 0));
		frc::sim::DriverStationSim::SetJoystickIsXbox(id,
				joystickTable.get("Xbox", false));
		frc::sim::DriverStationSim::SetJoystickButtonCount(id,
				joystickTable.get("ButtonCount", 0));
		frc::sim::DriverStationSim::SetJoystickButtons(id,
				joystickTable.get("ButtonValues", 0));

		std::vector<long> povValues = joystickTable.get("POVs",
				std::vector<long> { });
		frc::sim::DriverStationSim::SetJoystickPOVCount(id, povValues.size());
		for (size_t i = 0; i < povValues.size(); i++)
			frc::sim::DriverStationSim::SetJoystickPOV(id, i, povValues[i]);

		std::vector<float> axisValues = joystickTable.get("AxisValues",
				std::vector<float> { });
		std::vector<long> axisTypes = joystickTable.get("AxisTypes",
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
