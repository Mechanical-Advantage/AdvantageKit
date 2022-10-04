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

#define MAX_CHANNEL_COUNT 20

SystemReader::SystemReader() : is_running(false) {}

SystemReader::~SystemReader() {
  if (is_running) {
    is_running = false;    // Stop the thread when we destruct this object
    system_thread.join();
  }
}

void SystemReader::start() {
  system_thread = std::thread(&SystemReader::update_system_data, this);
}

void SystemReader::update_system_data() {
  is_running = true;
  while (is_running) {
    // We should wait on something... DS packet?
    HAL_WaitForDSData();
    
    std::int32_t status;
    double voltage_vin = HAL_GetVinVoltage(&status);
    double current_vin = HAL_GetVinCurrent(&status);

    double user_voltage_3v3 = HAL_GetUserVoltage3V3(&status);
    double user_current_3v3 = HAL_GetUserCurrent3V3(&status);
    HAL_Bool user_active_3v3 = HAL_GetUserActive3V3(&status);
    int32_t user_current_faults_3v3 = HAL_GetUserCurrentFaults3V3(&status);

    double user_voltage_5v = HAL_GetUserVoltage5V(&status);
    double user_current_5v = HAL_GetUserCurrent5V(&status);
    HAL_Bool user_active_5v = HAL_GetUserActive5V(&status);
    int32_t user_current_faults_5v = HAL_GetUserCurrentFaults5V(&status);

    double user_voltage_6v = HAL_GetUserVoltage6V(&status);
    double user_current_6v = HAL_GetUserCurrent6V(&status);
    HAL_Bool user_active_6v = HAL_GetUserActive6V(&status);
    int32_t user_current_faults_6v = HAL_GetUserCurrentFaults6V(&status);

    HAL_Bool browned_out = HAL_GetBrownedOut(&status);
    HAL_Bool system_active = HAL_GetSystemActive(&status);

    float percent_bus_utilization = 0;
    uint32_t bus_off_count = 0;
    uint32_t tx_full_count = 0;
    uint32_t receive_error_count = 0;
    uint32_t transmit_error_count = 0;
    HAL_CAN_GetCANStatus(
      &percent_bus_utilization, 
      &bus_off_count, 
      &tx_full_count,
      &receive_error_count, 
      &transmit_error_count, 
      &status);
    
    uint64_t systemTime = wpi::GetSystemTime();

    // Copy all data into the internal buffer
    copy_mutex.lock();

    internal_buf.mutate_voltage_vin(voltage_vin);
    internal_buf.mutate_current_vin(current_vin);

    internal_buf.mutate_user_voltage_3v3(user_voltage_3v3);
    internal_buf.mutate_user_current_3v3(user_current_3v3);
    internal_buf.mutate_user_active_3v3(user_active_3v3);
    internal_buf.mutate_user_current_faults_3v3(user_current_faults_3v3);

    internal_buf.mutate_user_voltage_5v(user_voltage_5v);
    internal_buf.mutate_user_current_5v(user_current_5v);
    internal_buf.mutate_user_active_5v(user_active_5v);
    internal_buf.mutate_user_current_faults_5v(user_current_faults_5v);

    internal_buf.mutate_user_voltage_6v(user_voltage_6v);
    internal_buf.mutate_user_current_6v(user_current_6v);
    internal_buf.mutate_user_active_6v(user_active_6v);
    internal_buf.mutate_user_current_faults_6v(user_current_faults_6v);

    internal_buf.mutate_browned_out(browned_out);
    internal_buf.mutate_system_active(system_active);
    internal_buf.mutate_epoch_time(systemTime);

    auto canStatus = internal_buf.mutable_can_status();
    canStatus.mutate_percent_bus_utilization(percent_bus_utilization);
    canStatus.mutate_bus_off_count(bus_off_count);
    canStatus.mutate_tx_full_count(tx_full_count);
    canStatus.mutate_receive_error_count(receive_error_count);
    canStatus.mutate_transmit_error_count(transmit_error_count);

    copy_mutex.unlock();
  }
}

void SystemReader::read(schema::SystemData* system_buf) {
  if (copy_mutex.try_lock_for(5s)) {
    std::memcpy(system_buf, &internal_buf, sizeof(schema::SystemData));
    copy_mutex.unlock();
  } else {
    std::cout
        << "[conduit] Could not acquire System Stats read lock after 5 seconds!  Exiting!"
        << std::endl;
    exit(1);
  }
}