#include "conduit/wpilibio/include/system_reader.h"

#include <hal/PowerDistribution.h>
#include <hal/HALBase.h>
#include <wpi/StackTrace.h>
#include <hal/DriverStation.h>
#include <wpi/timestamp.h>
#include <hal/CAN.h>
#include <hal/Power.h>

#include <chrono>
#include <cstdint>
#include <cstring>
#include <iostream>
#include <mutex>

using namespace std::chrono_literals;

void SystemReader::read(schema::SystemData* system_buf) {
  std::int32_t status;

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

  system_buf->mutate_browned_out(HAL_GetBrownedOut(&status));
  system_buf->mutate_system_active(HAL_GetSystemActive(&status));
  system_buf->mutate_epoch_time(wpi::GetSystemTime());

  float percent_bus_utilization = 0;
  uint32_t bus_off_count = 0;
  uint32_t tx_full_count = 0;
  uint32_t receive_error_count = 0;
  uint32_t transmit_error_count = 0;
  HAL_CAN_GetCANStatus(&percent_bus_utilization, &bus_off_count, &tx_full_count,
                       &receive_error_count, &transmit_error_count, &status);

  system_buf->mutable_can_status().mutate_percent_bus_utilization(
      percent_bus_utilization);
  system_buf->mutable_can_status().mutate_bus_off_count(bus_off_count);
  system_buf->mutable_can_status().mutate_tx_full_count(tx_full_count);
  system_buf->mutable_can_status().mutate_receive_error_count(
      receive_error_count);
  system_buf->mutable_can_status().mutate_transmit_error_count(
      transmit_error_count);
}