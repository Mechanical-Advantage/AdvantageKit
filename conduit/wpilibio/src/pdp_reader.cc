#include "conduit/wpilibio/include/pdp_reader.h"

#include <hal/PowerDistribution.h>
#include <hal/HALBase.h>
#include <wpi/StackTrace.h>

#include <chrono>
#include <cstdint>
#include <cstring>
#include <iostream>
#include <mutex>

using namespace std::chrono_literals;

#define MAX_CHANNEL_COUNT 20

PDPReader::PDPReader() : is_running(false) {}

PDPReader::~PDPReader() {
  if (is_running) {
    is_running = false;    // Stop the thread when we destruct this object
    pdp_thread.join();
  }
}

void PDPReader::start() {
  auto stack = wpi::GetStackTrace(1);
  
  std::int32_t status;
  pdp_handle = HAL_InitializePowerDistribution(-1, HAL_PowerDistributionType_kAutomatic, stack.c_str(), &status);

  channel_count = HAL_GetPowerDistributionNumChannels(pdp_handle, &status);
  pdp_thread = std::thread(&PDPReader::update_pdp_data, this);
}

void PDPReader::update_pdp_data() {
  is_running = true;
  while (is_running) {
    std::int32_t status;

    double temperature = HAL_GetPowerDistributionTemperature(pdp_handle, &status);
    
    double voltage = HAL_GetPowerDistributionVoltage(pdp_handle, &status);

    int32_t channel_count = HAL_GetPowerDistributionNumChannels(pdp_handle, &status);

    double channel_currents[MAX_CHANNEL_COUNT];
    HAL_GetPowerDistributionAllChannelCurrents(pdp_handle, channel_currents, channel_count, &status);

    double total_current = HAL_GetPowerDistributionTotalCurrent(pdp_handle, &status);
    double total_power = HAL_GetPowerDistributionTotalPower(pdp_handle, &status);
    double total_energy = HAL_GetPowerDistributionTotalEnergy(pdp_handle, &status);

    // Copy all data into the internal buffer
    copy_mutex.lock();

    internal_buf.mutate_temperature(temperature);
    internal_buf.mutate_voltage(voltage);
    internal_buf.mutate_channel_count(channel_count);

    flatbuffers::Array<double, 20> *current_buf = internal_buf.mutable_channel_current();
    for (int32_t i = 0; i < channel_count; i++)
    {
      current_buf->Mutate(i, channel_currents[i]);
    }

    internal_buf.mutate_total_current(total_current);
    internal_buf.mutate_total_power(total_power);
    internal_buf.mutate_total_energy(total_energy);

    copy_mutex.unlock();
  }
}

void PDPReader::read(schema::PDPData* pdp_buf) {
  if (copy_mutex.try_lock_for(5s)) {
    std::memcpy(pdp_buf, &internal_buf, sizeof(schema::PDPData));
    copy_mutex.unlock();
  } else {
    std::cout
        << "[conduit] Could not acquire PDP read lock after 5 seconds!  Exiting!"
        << std::endl;
    exit(1);
  }
}