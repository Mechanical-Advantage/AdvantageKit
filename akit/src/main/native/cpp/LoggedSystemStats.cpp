// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include "akit/LoggedSystemStats.h"
#include "akit/conduit/ConduitApi.h"

using namespace akit;

std::unordered_set<std::string> LoggedSystemStats::lastNTRemoteIds;
std::array<std::byte, 4> LoggedSystemStats::ntIntBuffer;

void LoggedSystemStats::SaveToLog(LogTable &&table) {
	conduit::ConduitApi &inst = conduit::ConduitApi::getInstance();

	table.Put("FPGAVersion", inst.getFPGAVersion());
	table.Put("FPGARevision", inst.getFPGARevision());
	table.Put("SerialNumber", inst.getSerialNumber());
	table.Put("Comments", inst.getComments());
	table.Put("TeamNumber", inst.getTeamNumber());
	table.Put("FPGAButton", inst.getFPGAButton());
	table.Put("SystemActive", inst.getSystemActive());
	table.Put("BrownedOut", inst.getBrownedOut());
	table.Put("CommsDisableCount", inst.getCommsDisableCount());
	table.Put("RSLState", inst.getRSLState());
	table.Put("SystemTimeValid", inst.getSystemTimeValid());

	table.Put("BatteryVoltage", inst.getVoltageVin());
	table.Put("BatteryCurrent", inst.getCurrentVin());

	table.Put("3v3Rail/Voltage", inst.getUserVoltage3v3());
	table.Put("3v3Rail/Current", inst.getUserCurrent3v3());
	table.Put("3v3Rail/Active", inst.getUserActive3v3());
	table.Put("3v3Rail/CurrentFaults", inst.getUserCurrentFaults3v3());

	table.Put("5vRail/Voltage", inst.getUserVoltage5v());
	table.Put("5vRail/Current", inst.getUserCurrent5v());
	table.Put("5vRail/Active", inst.getUserActive5v());
	table.Put("5vRail/CurrentFaults", inst.getUserCurrentFaults5v());

	table.Put("6vRail/Voltage", inst.getUserVoltage6v());
	table.Put("6vRail/Current", inst.getUserCurrent6v());
	table.Put("6vRail/Active", inst.getUserActive6v());
	table.Put("6vRail/CurrentFaults", inst.getUserCurrentFaults6v());

	table.Put("BrownoutVoltage", inst.getBrownoutVoltage());
	table.Put("CPUTempCelsius", inst.getCPUTemp());

	table.Put("CANBus/Utilization", inst.getCANBusUtilization());
	table.Put("CANBus/OffCount", inst.getBusOffCount());
	table.Put("CANBus/TxFullCount", inst.getTxFullCount());
	table.Put("CANBus/ReceiveErrorCount", inst.getReceiveErrorCount());
	table.Put("CANBus/TransmitErrorCount", inst.getTransmitErrorCount());

	table.Put("EpochTimeMicros", inst.getEpochTime());

	LogTable ntClientsTable = table.GetSubtable("NTClients");
	std::vector < nt::ConnectionInfo > ntConnections =
			nt::NetworkTableInstance::GetDefault().GetConnections();
	std::unordered_set < std::string > ntRemoteIds;

	for (size_t i = 0; i < ntConnections.size(); i++) {
		lastNTRemoteIds.erase(ntConnections[i].remote_id);
		ntRemoteIds.insert(ntConnections[i].remote_id);
		LogTable ntClientTable = ntClientsTable.GetSubtable(
				ntConnections[i].remote_id);

		ntClientTable.Put("Connected", true);
		ntClientTable.Put("IPAddress", ntConnections[i].remote_ip);
		ntClientTable.Put("RemotePort", ntConnections[i].remote_port);
		ntIntBuffer = std::bit_cast<std::array<std::byte, 4>>(
				ntConnections[i].protocol_version);
		ntClientTable.Put("ProtocolVersion", std::vector<std::byte> {
				ntIntBuffer.begin(), ntIntBuffer.end() });
	}
}
