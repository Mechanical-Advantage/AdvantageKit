// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include "conduit/system_reader.h"

#include <hal/CAN.h>
#include <hal/DriverStation.h>
#include <hal/HALBase.h>
#include <hal/Ports.h>
#include <hal/Power.h>
#include <hal/PowerDistribution.h>
#include <wpi/StackTrace.h>
#include <wpi/timestamp.h>

#include <chrono>
#include <cstdint>
#include <cstring>
#include <iostream>
#include <mutex>

using namespace std::chrono_literals;

static const int NUM_CAN_BUSES = 5;

void SystemReader::read(schema::SystemData *system_buf) {
	std::int32_t status;

	// Update values that shouldn't change after initial cycle
	if (cycleCount == 0) {
		system_buf->mutate_fpga_version(HAL_GetFPGAVersion(&status));
		system_buf->mutate_fpga_revision(HAL_GetFPGARevision(&status));

		WPI_String serialNum;
		HAL_GetSerialNumber(&serialNum);
		system_buf->mutate_serial_number_size(serialNum.len);
		std::memcpy(system_buf->mutable_serial_number()->Data(), serialNum.str,
				serialNum.len);

		WPI_String comments;
		HAL_GetComments(&comments);
		system_buf->mutate_comments_size(comments.len);
		std::memcpy(system_buf->mutable_comments()->Data(), comments.str,
				comments.len);

		system_buf->mutate_team_number(HAL_GetTeamNumber());
	}

	system_buf->mutate_system_active(HAL_GetSystemActive(&status));
	system_buf->mutate_browned_out(HAL_GetBrownedOut(&status));
	system_buf->mutate_comms_disable_count(HAL_GetCommsDisableCount(&status));
	system_buf->mutate_rsl_state(HAL_GetRSLState(&status));
	if (cycleCount % 50 == 0) {
		// This read takes longer
		system_buf->mutate_system_time_valid(HAL_GetSystemTimeValid(&status));
	}

	system_buf->mutate_voltage_vin(HAL_GetVinVoltage(&status));

	system_buf->mutate_user_voltage_3v3(HAL_GetUserVoltage3V3(&status));
	system_buf->mutate_user_current_3v3(HAL_GetUserCurrent3V3(&status));
	system_buf->mutate_user_active_3v3(HAL_GetUserActive3V3(&status));
	system_buf->mutate_user_current_faults_3v3(
			HAL_GetUserCurrentFaults3V3(&status));

	system_buf->mutate_brownout_voltage(HAL_GetBrownoutVoltage(&status));
	system_buf->mutate_cpu_temp(HAL_GetCPUTemp(&status));
	system_buf->mutate_epoch_time(wpi::GetSystemTime());

	schema::CANStatus can_status_bufs[NUM_CAN_BUSES];

	for (int bus_id = 0; bus_id < NUM_CAN_BUSES; bus_id++) {
		float percent_bus_utilization = 0;
		uint32_t bus_off_count = 0;
		uint32_t tx_full_count = 0;
		uint32_t receive_error_count = 0;
		uint32_t transmit_error_count = 0;
		HAL_CAN_GetCANStatus(bus_id, &percent_bus_utilization, &bus_off_count,
				&tx_full_count, &receive_error_count, &transmit_error_count,
				&status);

		schema::CANStatus *can_status_buf = &can_status_bufs[bus_id];
		can_status_buf->mutate_percent_bus_utilization(percent_bus_utilization);
		can_status_buf->mutate_bus_off_count(bus_off_count);
		can_status_buf->mutate_tx_full_count(tx_full_count);
		can_status_buf->mutate_receive_error_count(receive_error_count);
		can_status_buf->mutate_transmit_error_count(transmit_error_count);
	}

	std::memcpy(system_buf->mutable_can_status()->Data(), can_status_bufs,
			system_buf->can_status()->size() * sizeof(schema::CANStatus));

	cycleCount++;
}
