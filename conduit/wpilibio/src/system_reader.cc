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
    double voltage = HAL_GetVinVoltage(&status);

    HAL_Bool browned_out = HAL_GetBrownedOut(&status);

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

    internal_buf.mutate_voltage(voltage);
    internal_buf.mutate_browned_out(browned_out);
    internal_buf.mutate_can_bus_utilization(percent_bus_utilization);
    internal_buf.mutate_epoch_time(systemTime);

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