#include "conduit/wpilibio/include/pdp_reader.h"
#include "conduit/wpilibio/include/HALUtil.h"
#include "conduit/wpilibio/include/pdp_util.h"
#include "conduit/wpilibio/include/pdh_util.h"

#include <hal/PowerDistribution.h>
#include <hal/HALBase.h>
#include <wpi/StackTrace.h>
#include <hal/DriverStation.h>
#include <wpi/jni_util.h>
#include <hal/CANAPI.h>

#include <chrono>
#include <cstdint>
#include <cstring>
#include <iostream>
#include <mutex>

using namespace std::chrono_literals;

#define MAX_CHANNEL_COUNT 24


PDPReader::PDPReader() : is_running(false) {}

PDPReader::~PDPReader() {
  if (is_running) {
    is_running = false;    // Stop the thread when we destruct this object
    pdp_thread.join();
  }
}

void PDPReader::start() {
  pdp_thread = std::thread(&PDPReader::update_pd_data, this);
}

void PDPReader::configure(JNIEnv *env, jint module, jint type)
{
  should_run = false;

  int32_t status = 0;
  auto stack = wpi::java::GetJavaStackTrace(env, "edu.wpi.first");
  pd_handle = HAL_InitializePowerDistribution(module, static_cast<HAL_PowerDistributionType>(type), stack.c_str(), &status);
  hal::CheckStatusForceThrow(env, status);


  pd_module_id = HAL_GetPowerDistributionModuleNumber(pd_handle, &status);
  hal::CheckStatus(env, status, false);

  pd_type = HAL_GetPowerDistributionType(pd_handle, &status);
  hal::CheckStatus(env, status, false);

  runtime = HAL_GetRuntimeType();

  if (runtime != HAL_Runtime_Simulation) {
    if (pd_type == HAL_PowerDistributionType_kCTRE) {
      pd_can_handle = HAL_InitializeCAN(HAL_CAN_Man_kCTRE, pd_module_id, HAL_CAN_Dev_kPowerDistribution, &status);
    } else if (pd_type == HAL_PowerDistributionType_kRev) {
      pd_can_handle = HAL_InitializeCAN(HAL_CAN_Man_kREV, pd_module_id, HAL_CAN_Dev_kPowerDistribution, &status);
    }
  }

  copy_mutex.lock();

  internal_buf.mutate_handle(pd_handle);
  internal_buf.mutate_type(pd_type);
  internal_buf.mutate_module_id(pd_module_id);
  if (runtime == HAL_Runtime_Simulation) {
    internal_buf.mutate_channel_count(24);
  } else if (pd_type == HAL_PowerDistributionType_kCTRE) {
    internal_buf.mutate_channel_count(16);
  } else if (pd_type == HAL_PowerDistributionType_kRev) {
    internal_buf.mutate_channel_count(24);
  }

  copy_mutex.unlock();

  should_run = true;
}

void PDPReader::update_pd_data() {
  is_running = true;
  int32_t status;

  while (is_running) {
    auto now = HAL_GetFPGATime(&status) * 1us;
    std::this_thread::sleep_for(5ms - (now - timestamp));
    timestamp = HAL_GetFPGATime(&status) * 1us;
    loop_counter += 5;
    loop_counter %= 100; // repeat every 100ms

    if (!should_run) {
      continue;
    }

    if (runtime == HAL_Runtime_Simulation) {
      update_sim_data(loop_counter);
    } else if (pd_type == HAL_PowerDistributionType_kCTRE) {
      update_ctre_pdp_data(loop_counter);
    } else if (pd_type == HAL_PowerDistributionType_kRev) {
      update_rev_pdh_data(loop_counter);
    }
  }
}

void PDPReader::update_ctre_pdp_data(uint8_t loop_counter) {
  std::int32_t status;

  if (loop_counter % 25 == 0) {
    // Read Status 1, 2, 3 (0x50, 0x51, 0x52)
    double channel_currents[MAX_CHANNEL_COUNT];
    double temperature;
    double voltage;

    {
      PdpStatus1 pdpStatus1;
      int32_t length;
      uint64_t timestamp;
      HAL_ReadCANPacketLatest(pd_can_handle, PDP_STATUS_1, pdpStatus1.data, &length, &timestamp, &status);

      PdpStatus1Result status1;
      parseStatusFrame1(pdpStatus1, status1);

      channel_currents[0] = status1.chan1 * 0.125;
      channel_currents[1] = status1.chan2 * 0.125;
      channel_currents[2] = status1.chan3 * 0.125;
      channel_currents[3] = status1.chan4 * 0.125;
      channel_currents[4] = status1.chan5 * 0.125;
      channel_currents[5] = status1.chan6 * 0.125;

      if (status == 0) {
        // Copy all data into the internal buffer
        copy_mutex.lock();

        auto current_buf = internal_buf.mutable_channel_current();

        current_buf->Mutate(0, channel_currents[0]);
        current_buf->Mutate(1, channel_currents[1]);
        current_buf->Mutate(2, channel_currents[2]);
        current_buf->Mutate(3, channel_currents[3]);
        current_buf->Mutate(4, channel_currents[4]);
        current_buf->Mutate(5, channel_currents[5]);

        copy_mutex.lock();
      }
    }

    {
      PdpStatus2 pdpStatus2;
      int32_t length;
      uint64_t timestamp;
      HAL_ReadCANPacketLatest(pd_can_handle, PDP_STATUS_2, pdpStatus2.data, &length, &timestamp, &status);

      PdpStatus2Result status2;
      parseStatusFrame2(pdpStatus2, status2);

      channel_currents[6] = status2.chan7 * 0.125;
      channel_currents[7] = status2.chan8 * 0.125;
      channel_currents[8] = status2.chan9 * 0.125;
      channel_currents[9] = status2.chan10 * 0.125;
      channel_currents[10] = status2.chan11 * 0.125;
      channel_currents[11] = status2.chan12 * 0.125;

      if (status == 0) {
        // Copy all data into the internal buffer
        copy_mutex.lock();

        auto current_buf = internal_buf.mutable_channel_current();

        current_buf->Mutate(6, channel_currents[0]);
        current_buf->Mutate(7, channel_currents[1]);
        current_buf->Mutate(8, channel_currents[2]);
        current_buf->Mutate(9, channel_currents[3]);
        current_buf->Mutate(10, channel_currents[4]);
        current_buf->Mutate(11, channel_currents[5]);

        copy_mutex.lock();
      }
    }

    {
      PdpStatus3 pdpStatus3;
      int32_t length;
      uint64_t timestamp;
      HAL_ReadCANPacketLatest(pd_can_handle, PDP_STATUS_3, pdpStatus3.data, &length, &timestamp, &status);

      PdpStatus3Result status3;
      parseStatusFrame3(pdpStatus3, status3);

      channel_currents[12] = status3.chan13 * 0.125;
      channel_currents[13] = status3.chan14 * 0.125;
      channel_currents[14] = status3.chan15 * 0.125;
      channel_currents[15] = status3.chan16 * 0.125;
      temperature = status3.temp * 1.03250836957542 - 67.8564500484966;
      voltage = status3.busVoltage * 0.05 + 4.0;

      if (status == 0) {
        // Copy all data into the internal buffer
        copy_mutex.lock();

        internal_buf.mutate_temperature(temperature);
        internal_buf.mutate_voltage(voltage);

        auto current_buf = internal_buf.mutable_channel_current();

        current_buf->Mutate(12, channel_currents[12]);
        current_buf->Mutate(13, channel_currents[13]);
        current_buf->Mutate(14, channel_currents[14]);
        current_buf->Mutate(15, channel_currents[15]);

        copy_mutex.lock();
      }
    }
  }

   if (loop_counter % 20 == 0) {
    // Read StatusEnergy (0x5D)
    double total_current;
    double total_power;
    double total_energy;

    PdpStatusEnergy pdpStatus;
    int32_t length;
    uint64_t timestamp;
    HAL_ReadCANPacketLatest(pd_can_handle, PDP_STATUS_ENERGY, pdpStatus.data, &length, &timestamp, &status);

    PdpStatusEnergyResult energy;
    parseStatusFrameEnergy(pdpStatus, energy);

    total_current = energy.totalCurrent * 0.125;
    total_power = energy.totalPower * 0.125;
    total_energy = energy.totalEnergy * 0.125 * 0.001 * energy.TmeasMs_likelywillbe20ms_;

    if (status == 0) {
      // Copy all data into the internal buffer
      copy_mutex.lock();

      internal_buf.mutate_total_current(total_current);
      internal_buf.mutate_total_power(total_power);
      internal_buf.mutate_total_energy(total_energy);

      copy_mutex.unlock();
    }
  }
}


void PDPReader::update_rev_pdh_data(uint8_t loop_counter) {
  std::int32_t status;

  uint8_t data[8];
  int32_t length;
  uint64_t timestamp;
  // I don't know the frequency of these can frames; it is not published

  double channel_currents[MAX_CHANNEL_COUNT];
  int32_t faults = 0;
  int32_t sticky_faults = 0;
  double voltage = 0;
  double temp = 0;
  // bool enabled = false;
  // bool switch_channel = false;
  double total_current;
  double total_power = 0;
  double total_energy = 0;


  HAL_ReadCANPacketLatest(pd_can_handle, PDH_STATUS_0_API_ID, data, &length, &timestamp, &status);

  PDH_status_0_t status0;
  PDH_status_0_unpack(&status0, data, length);

  channel_currents[0] = status0.channel_0_current * 0.125;
  channel_currents[1] = status0.channel_1_current * 0.125;
  channel_currents[2] = status0.channel_2_current * 0.125;
  channel_currents[3] = status0.channel_3_current * 0.125;
  channel_currents[4] = status0.channel_4_current * 0.125;
  channel_currents[5] = status0.channel_5_current * 0.125;

  faults |= status0.channel_0_breaker_fault << 0;
  faults |= status0.channel_1_breaker_fault << 1;
  faults |= status0.channel_2_breaker_fault << 2;
  faults |= status0.channel_3_breaker_fault << 3;

  if (status == 0) {
    // Copy all data into the internal buffer
    copy_mutex.lock();

    auto currents = internal_buf.mutable_channel_current();

    currents->Mutate(0, channel_currents[0]);
    currents->Mutate(1, channel_currents[1]);
    currents->Mutate(2, channel_currents[2]);
    currents->Mutate(3, channel_currents[3]);
    currents->Mutate(3, channel_currents[4]);
    currents->Mutate(3, channel_currents[5]);

    auto mut_faults = internal_buf.faults();
    mut_faults = (mut_faults & 0xFFFFFFF0) | (mut_faults & faults);

    internal_buf.mutate_faults(mut_faults);

    copy_mutex.unlock();
  }


  HAL_ReadCANPacketLatest(pd_can_handle, PDH_STATUS_1_API_ID, data, &length, &timestamp, &status);

  PDH_status_1_t status1;
  PDH_status_1_unpack(&status1, data, length);

  channel_currents[6] = status1.channel_6_current * 0.125;
  channel_currents[7] = status1.channel_7_current * 0.125;
  channel_currents[8] = status1.channel_8_current * 0.125;
  channel_currents[9] = status1.channel_9_current * 0.125;
  channel_currents[10] = status1.channel_10_current * 0.125;
  channel_currents[11] = status1.channel_11_current * 0.125;

  faults |= status1.channel_4_breaker_fault << 4;
  faults |= status1.channel_5_breaker_fault << 5;
  faults |= status1.channel_6_breaker_fault << 6;
  faults |= status1.channel_7_breaker_fault << 7;

  if (status == 0) {
    // Copy all data into the internal buffer
    copy_mutex.lock();

    auto currents = internal_buf.mutable_channel_current();

    currents->Mutate(6, channel_currents[6]);
    currents->Mutate(7, channel_currents[7]);
    currents->Mutate(8, channel_currents[8]);
    currents->Mutate(9, channel_currents[9]);
    currents->Mutate(10, channel_currents[10]);
    currents->Mutate(11, channel_currents[11]);

    auto mut_faults = internal_buf.faults();
    mut_faults = (mut_faults & 0xFFFFFFF0F) | (mut_faults & faults);

    internal_buf.mutate_faults(mut_faults);

    copy_mutex.unlock();
  }


  HAL_ReadCANPacketLatest(pd_can_handle, PDH_STATUS_2_API_ID, data, &length, &timestamp, &status);

  PDH_status_2_t status2;
  PDH_status_2_unpack(&status2, data, length);

  channel_currents[12] = status2.channel_12_current * 0.125;
  channel_currents[13] = status2.channel_13_current * 0.125;
  channel_currents[14] = status2.channel_14_current * 0.125;
  channel_currents[15] = status2.channel_15_current * 0.125;
  channel_currents[16] = status2.channel_16_current * 0.125;
  channel_currents[17] = status2.channel_17_current * 0.125;

  faults |= status2.channel_8_breaker_fault << 8;
  faults |= status2.channel_9_breaker_fault << 9;
  faults |= status2.channel_10_breaker_fault << 10;
  faults |= status2.channel_11_breaker_fault << 11;

  if (status == 0) {
    // Copy all data into the internal buffer
    copy_mutex.lock();

    auto currents = internal_buf.mutable_channel_current();

    currents->Mutate(12, channel_currents[12]);
    currents->Mutate(13, channel_currents[13]);
    currents->Mutate(14, channel_currents[14]);
    currents->Mutate(15, channel_currents[15]);
    currents->Mutate(16, channel_currents[16]);
    currents->Mutate(17, channel_currents[17]);

    auto mut_faults = internal_buf.faults();
    mut_faults = (mut_faults & 0xFFFFFF0FF) | (mut_faults & faults);

    internal_buf.mutate_faults(mut_faults);

    copy_mutex.unlock();
  }


  HAL_ReadCANPacketLatest(pd_can_handle, PDH_STATUS_3_API_ID, data, &length, &timestamp, &status);

  PDH_status_3_t status3;
  PDH_status_3_unpack(&status3, data, length);

  channel_currents[18] = status2.channel_12_current * 0.125;
  channel_currents[19] = status2.channel_13_current * 0.125;
  channel_currents[20] = status2.channel_14_current * 0.0625;
  channel_currents[21] = status2.channel_15_current * 0.0625;
  channel_currents[22] = status2.channel_16_current * 0.0625;
  channel_currents[23] = status2.channel_17_current * 0.0625;

  faults |= status3.channel_12_breaker_fault << 12;
  faults |= status3.channel_13_breaker_fault << 13;
  faults |= status3.channel_14_breaker_fault << 14;
  faults |= status3.channel_15_breaker_fault << 15;
  faults |= status3.channel_16_breaker_fault << 16;
  faults |= status3.channel_17_breaker_fault << 17;
  faults |= status3.channel_18_breaker_fault << 18;
  faults |= status3.channel_19_breaker_fault << 19;
  faults |= status3.channel_20_breaker_fault << 20;
  faults |= status3.channel_21_breaker_fault << 21;
  faults |= status3.channel_22_breaker_fault << 22;
  faults |= status3.channel_23_breaker_fault << 23;

  if (status == 0) {
    // Copy all data into the internal buffer
    copy_mutex.lock();

    auto currents = internal_buf.mutable_channel_current();

    currents->Mutate(18, channel_currents[18]);
    currents->Mutate(19, channel_currents[19]);
    currents->Mutate(20, channel_currents[20]);
    currents->Mutate(21, channel_currents[21]);
    currents->Mutate(22, channel_currents[22]);
    currents->Mutate(23, channel_currents[23]);

    auto mut_faults = internal_buf.faults();
    mut_faults = (mut_faults & 0xFF000FFF) | (mut_faults & faults);

    internal_buf.mutate_faults(mut_faults);

    copy_mutex.unlock();
  }


  HAL_ReadCANPacketLatest(pd_can_handle, PDH_STATUS_4_API_ID, data, &length, &timestamp, &status);

  PDH_status_4_t status4;
  PDH_status_4_unpack(&status4, data, length);

  voltage = status4.v_bus * 0.0078125;
  // enabled = status4.system_enable;
  // switch_channel = status4.switch_channel_state;
  total_current = status4.total_current * 2;

  faults |= status4.brownout_fault << 24;
  faults |= status4.can_warning_fault << 25;
  faults |= status4.hardware_fault << 26;

  sticky_faults |= status4.sticky_ch0_breaker_fault << 0;
  sticky_faults |= status4.sticky_ch1_breaker_fault << 1;
  sticky_faults |= status4.sticky_ch2_breaker_fault << 2;
  sticky_faults |= status4.sticky_ch3_breaker_fault << 3;
  sticky_faults |= status4.sticky_ch4_breaker_fault << 4;
  sticky_faults |= status4.sticky_ch5_breaker_fault << 5;
  sticky_faults |= status4.sticky_ch6_breaker_fault << 6;
  sticky_faults |= status4.sticky_ch7_breaker_fault << 7;
  sticky_faults |= status4.sticky_ch8_breaker_fault << 8;
  sticky_faults |= status4.sticky_ch9_breaker_fault << 9;
  sticky_faults |= status4.sticky_ch10_breaker_fault << 10;
  sticky_faults |= status4.sticky_ch11_breaker_fault << 11;
  sticky_faults |= status4.sticky_ch12_breaker_fault << 12;
  sticky_faults |= status4.sticky_ch13_breaker_fault << 13;
  sticky_faults |= status4.sticky_ch14_breaker_fault << 14;
  sticky_faults |= status4.sticky_ch15_breaker_fault << 15;
  sticky_faults |= status4.sticky_ch16_breaker_fault << 16;
  sticky_faults |= status4.sticky_ch17_breaker_fault << 17;
  sticky_faults |= status4.sticky_ch18_breaker_fault << 18;
  sticky_faults |= status4.sticky_ch19_breaker_fault << 19;
  sticky_faults |= status4.sticky_ch20_breaker_fault << 20;
  sticky_faults |= status4.sticky_ch21_breaker_fault << 21;
  sticky_faults |= status4.sticky_ch22_breaker_fault << 22;
  sticky_faults |= status4.sticky_ch23_breaker_fault << 23;
  sticky_faults |= status4.sticky_brownout_fault << 24;
  sticky_faults |= status4.sticky_can_warning_fault << 25;
  sticky_faults |= status4.sticky_can_bus_off_fault << 26;
  sticky_faults |= status4.sticky_has_reset_fault << 27;
  sticky_faults |= status4.sticky_hardware_fault << 28;
  sticky_faults |= status4.sticky_firmware_fault << 29;

  if (status == 0) {
    // Copy all data into the internal buffer
    copy_mutex.lock();

    internal_buf.mutate_voltage(voltage);
    internal_buf.mutate_total_current(total_current);
    internal_buf.mutate_sticky_faults(sticky_faults);

    auto mut_faults = internal_buf.faults();
    mut_faults = (mut_faults & 0xF8FFFFFF) | (mut_faults & faults);

    internal_buf.mutate_faults(mut_faults);

    internal_buf.mutate_temperature(temp);
    internal_buf.mutate_total_energy(total_energy);
    internal_buf.mutate_total_power(total_power);

    copy_mutex.unlock();
  }

}

void PDPReader::update_sim_data(uint8_t loop_counter) {
  if (loop_counter % 20 != 0) {
    return;
  }

  int32_t status;

  HAL_PowerDistributionFaults faults;
  uint32_t faults_bits = 0;
  HAL_GetPowerDistributionFaults(pd_handle, &faults, &status);

  faults_bits |= faults.channel0BreakerFault << 0;
  faults_bits |= faults.channel1BreakerFault << 1;
  faults_bits |= faults.channel2BreakerFault << 2;
  faults_bits |= faults.channel3BreakerFault << 3;
  faults_bits |= faults.channel4BreakerFault << 4;
  faults_bits |= faults.channel5BreakerFault << 5;
  faults_bits |= faults.channel6BreakerFault << 6;
  faults_bits |= faults.channel7BreakerFault << 7;
  faults_bits |= faults.channel8BreakerFault << 8;
  faults_bits |= faults.channel9BreakerFault << 9;
  faults_bits |= faults.channel10BreakerFault << 10;
  faults_bits |= faults.channel11BreakerFault << 11;
  faults_bits |= faults.channel12BreakerFault << 12;
  faults_bits |= faults.channel13BreakerFault << 13;
  faults_bits |= faults.channel14BreakerFault << 14;
  faults_bits |= faults.channel15BreakerFault << 15;
  faults_bits |= faults.channel16BreakerFault << 16;
  faults_bits |= faults.channel17BreakerFault << 17;
  faults_bits |= faults.channel18BreakerFault << 18;
  faults_bits |= faults.channel19BreakerFault << 19;
  faults_bits |= faults.channel20BreakerFault << 20;
  faults_bits |= faults.channel21BreakerFault << 21;
  faults_bits |= faults.channel22BreakerFault << 22;
  faults_bits |= faults.channel23BreakerFault << 23;
  faults_bits |= faults.brownout << 24;
  faults_bits |= faults.canWarning << 25;
  faults_bits |= faults.hardwareFault << 26;

  HAL_PowerDistributionStickyFaults sticky_faults;
  uint32_t sticky_faults_bits = 0;
  HAL_GetPowerDistributionStickyFaults(pd_handle, &sticky_faults, &status);

  sticky_faults_bits |= sticky_faults.channel0BreakerFault << 0;
  sticky_faults_bits |= sticky_faults.channel1BreakerFault << 1;
  sticky_faults_bits |= sticky_faults.channel2BreakerFault << 2;
  sticky_faults_bits |= sticky_faults.channel3BreakerFault << 3;
  sticky_faults_bits |= sticky_faults.channel4BreakerFault << 4;
  sticky_faults_bits |= sticky_faults.channel5BreakerFault << 5;
  sticky_faults_bits |= sticky_faults.channel6BreakerFault << 6;
  sticky_faults_bits |= sticky_faults.channel7BreakerFault << 7;
  sticky_faults_bits |= sticky_faults.channel8BreakerFault << 8;
  sticky_faults_bits |= sticky_faults.channel9BreakerFault << 9;
  sticky_faults_bits |= sticky_faults.channel10BreakerFault << 10;
  sticky_faults_bits |= sticky_faults.channel11BreakerFault << 11;
  sticky_faults_bits |= sticky_faults.channel12BreakerFault << 12;
  sticky_faults_bits |= sticky_faults.channel13BreakerFault << 13;
  sticky_faults_bits |= sticky_faults.channel14BreakerFault << 14;
  sticky_faults_bits |= sticky_faults.channel15BreakerFault << 15;
  sticky_faults_bits |= sticky_faults.channel16BreakerFault << 16;
  sticky_faults_bits |= sticky_faults.channel17BreakerFault << 17;
  sticky_faults_bits |= sticky_faults.channel18BreakerFault << 18;
  sticky_faults_bits |= sticky_faults.channel19BreakerFault << 19;
  sticky_faults_bits |= sticky_faults.channel20BreakerFault << 20;
  sticky_faults_bits |= sticky_faults.channel21BreakerFault << 21;
  sticky_faults_bits |= sticky_faults.channel22BreakerFault << 22;
  sticky_faults_bits |= sticky_faults.channel23BreakerFault << 23;
  sticky_faults_bits |= sticky_faults.brownout << 24;
  sticky_faults_bits |= sticky_faults.canWarning << 25;
  sticky_faults_bits |= sticky_faults.canBusOff << 26;
  sticky_faults_bits |= sticky_faults.hasReset << 27;

  double channel_current[24];
  HAL_GetPowerDistributionAllChannelCurrents(pd_handle, channel_current, 24, &status);

  double temperature = HAL_GetPowerDistributionTemperature(pd_handle, &status);
  double voltage = HAL_GetPowerDistributionVoltage(pd_handle, &status);
  double total_current = HAL_GetPowerDistributionTotalCurrent(pd_handle, &status);
  double total_power = HAL_GetPowerDistributionTotalPower(pd_handle, &status);
  double total_energy = HAL_GetPowerDistributionTotalEnergy(pd_handle, &status);

  copy_mutex.lock();

  auto currents = internal_buf.mutable_channel_current();

  for (int i = 0; i < 24; i++) {
    currents->Mutate(i, channel_current[i]);
  }

  internal_buf.mutate_temperature(temperature);
  internal_buf.mutate_voltage(voltage);
  internal_buf.mutate_total_current(total_current);
  internal_buf.mutate_total_power(total_power);
  internal_buf.mutate_total_energy(total_energy);

  internal_buf.mutate_faults(faults_bits);
  internal_buf.mutate_sticky_faults(sticky_faults_bits);

  copy_mutex.unlock();
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