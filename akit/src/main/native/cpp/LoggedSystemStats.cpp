// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include "akit/LoggedSystemStats.h"
#include "akit/conduit/ConduitApi.h"

using namespace akit;

void LoggedSystemStats::saveToLog(LogTable &&table) {
	conduit::ConduitApi &inst = conduit::ConduitApi::getInstance();

	table.put("FPGAVersion", inst.getFPGAVersion());
	table.put("FPGARevision", inst.getFPGARevision());
	table.put("SerialNumber", inst.getSerialNumber());
	table.put("Comments", inst.getComments());
	table.put("TeamNumber", inst.getTeamNumber());
	table.put("FPGAButton", inst.getFPGAButton());
	table.put("SystemActive", inst.getSystemActive());
	table.put("BrownedOut", inst.getBrownedOut());
	table.put("CommsDisableCount", inst.getCommsDisableCount());
	table.put("RSLState", inst.getRSLState());
	table.put("SystemTimeValid", inst.getSystemTimeValid());

	table.put("BatteryVoltage", inst.getVoltageVin());
	table.put("BatteryCurrent", inst.getCurrentVin());

	table.put("3v3Rail/Voltage", inst.getUserVoltage3v3());
	table.put("3v3Rail/Current", inst.getUserCurrent3v3());
	table.put("3v3Rail/Active", inst.getUserActive3v3());
	table.put("3v3Rail/CurrentFaults", inst.getUserCurrentFaults3v3());

	table.put("5vRail/Voltage", inst.getUserVoltage5v());
	table.put("5vRail/Current", inst.getUserCurrent5v());
	table.put("5vRail/Active", inst.getUserActive5v());
	table.put("5vRail/CurrentFaults", inst.getUserCurrentFaults5v());

	table.put("6vRail/Voltage", inst.getUserVoltage6v());
	table.put("6vRail/Current", inst.getUserCurrent6v());
	table.put("6vRail/Active", inst.getUserActive6v());
	table.put("6vRail/CurrentFaults", inst.getUserCurrentFaults6v());

	table.put("BrownoutVoltage", inst.getBrownoutVoltage());
	table.put("CPUTempCelsius", inst.getCPUTemp());

	table.put("CANBus/Utilization", inst.getCANBusUtilization());
	table.put("CANBus/OffCount", inst.getBusOffCount());
	table.put("CANBus/TxFullCount", inst.getTxFullCount());
	table.put("CANBus/ReceiveErrorCount", inst.getReceiveErrorCount());
	table.put("CANBus/TransmitErrorCount", inst.getTransmitErrorCount());

	table.put("EpochTimeMicros", inst.getEpochTime());

	LogTable ntClientsTable = table.getSubtable("NTClients");
	std::vector < nt::ConnectionInfo > ntConnections =
			nt::NetworkTableInstance::GetDefault().GetConnections();
	std::unordered_set < std::string > ntRemoteIds;

	for (size_t i = 0; i < ntConnections.size(); i++) {
		lastNTRemoteIds.erase(ntConnections[i].remote_id);
		ntRemoteIds.insert(ntConnections[i].remote_id);
		LogTable ntClientTable = ntClientsTable.getSubtable(
				ntConnections[i].remote_id);

		ntClientTable.put("Connected", true);
		ntClientTable.put("IPAddress", ntConnections[i].remote_ip);
		ntClientTable.put("RemotePort", ntConnections[i].remote_port);
		ntIntBuffer = std::bit_cast<std::array<std::byte, 4>>(
				ntConnections[i].protocol_version);
		ntClientTable.put("ProtocolVersion", std::vector<std::byte>{ntIntBuffer.begin(), ntIntBuffer.end()});
	}
}
