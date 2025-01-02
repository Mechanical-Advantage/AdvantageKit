// Copyright 2021-2025 FRC 6328
// http://github.com/Mechanical-Advantage
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// version 3 as published by the Free Software Foundation or
// available in the root directory of this project.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.

#include "conduit/system_reader.h"

#include <hal/CAN.h>
#include <hal/DriverStation.h>
#include <hal/HALBase.h>
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

void SystemReader::read(schema::SystemData* system_buf) {
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

  system_buf->mutate_fpga_button(HAL_GetFPGAButton(&status));
  system_buf->mutate_system_active(HAL_GetSystemActive(&status));
  system_buf->mutate_browned_out(HAL_GetBrownedOut(&status));
  system_buf->mutate_comms_disable_count(HAL_GetCommsDisableCount(&status));
  system_buf->mutate_rsl_state(HAL_GetRSLState(&status));
  if (cycleCount % 50 == 0) {
    // This read takes longer
    system_buf->mutate_system_time_valid(HAL_GetSystemTimeValid(&status));
  }

  system_buf->mutate_voltage_vin(HAL_GetVinVoltage(&status));
  system_buf->mutate_current_vin(HAL_GetVinCurrent(&status));

  system_buf->mutate_user_voltage_3v3(HAL_GetUserVoltage3V3(&status));
  system_buf->mutate_user_current_3v3(HAL_GetUserCurrent3V3(&status));
  system_buf->mutate_user_active_3v3(HAL_GetUserActive3V3(&status));
  system_buf->mutate_user_current_faults_3v3(
      HAL_GetUserCurrentFaults3V3(&status));

  system_buf->mutate_user_voltage_5v(HAL_GetUserVoltage5V(&status));
  system_buf->mutate_user_current_5v(HAL_GetUserCurrent5V(&status));
  system_buf->mutate_user_active_5v(HAL_GetUserActive5V(&status));
  system_buf->mutate_user_current_faults_5v(
      HAL_GetUserCurrentFaults5V(&status));

  system_buf->mutate_user_voltage_6v(HAL_GetUserVoltage6V(&status));
  system_buf->mutate_user_current_6v(HAL_GetUserCurrent6V(&status));
  system_buf->mutate_user_active_6v(HAL_GetUserActive6V(&status));
  system_buf->mutate_user_current_faults_6v(
      HAL_GetUserCurrentFaults6V(&status));

  system_buf->mutate_brownout_voltage(HAL_GetBrownoutVoltage(&status));
  system_buf->mutate_cpu_temp(HAL_GetCPUTemp(&status));

  if (cycleCount % 20 == 0) {
    float percent_bus_utilization = 0;
    uint32_t bus_off_count = 0;
    uint32_t tx_full_count = 0;
    uint32_t receive_error_count = 0;
    uint32_t transmit_error_count = 0;
    HAL_CAN_GetCANStatus(&percent_bus_utilization, &bus_off_count,
                         &tx_full_count, &receive_error_count,
                         &transmit_error_count, &status);

    system_buf->mutable_can_status().mutate_percent_bus_utilization(
        percent_bus_utilization);
    system_buf->mutable_can_status().mutate_bus_off_count(bus_off_count);
    system_buf->mutable_can_status().mutate_tx_full_count(tx_full_count);
    system_buf->mutable_can_status().mutate_receive_error_count(
        receive_error_count);
    system_buf->mutable_can_status().mutate_transmit_error_count(
        transmit_error_count);
  }

  system_buf->mutate_epoch_time(wpi::GetSystemTime());
  cycleCount++;
}