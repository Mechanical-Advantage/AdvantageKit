// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include "conduit/pdp_reader.h"

#include <hal/CANAPI.h>
#include <hal/DriverStation.h>
#include <hal/HALBase.h>
#include <hal/PowerDistribution.h>
#include <wpi/StackTrace.h>
#include <wpi/jni_util.h>

#include <chrono>
#include <cstdint>
#include <cstring>
#include <iostream>
#include <mutex>

using namespace std::chrono_literals;

#define MAX_CHANNEL_COUNT 24

void PDPReader::configure(JNIEnv *env, jint bus, jint module, jint type,
		schema::PDPData *pdp_buf) {
	int32_t status = 0;
	auto stack = wpi::java::GetJavaStackTrace(env, "edu.wpi.first");
	pd_handle = HAL_InitializePowerDistribution(bus, module,
			static_cast<HAL_PowerDistributionType>(type), stack.c_str(),
			&status);
	int32_t pd_module_id = HAL_GetPowerDistributionModuleNumber(pd_handle,
			&status);
	pd_type = HAL_GetPowerDistributionType(pd_handle, &status);

	runtime = HAL_GetRuntimeType();
	if (runtime != HAL_Runtime_Simulation) {
		if (pd_type == HAL_PowerDistributionType_kCTRE) {
			pd_can_handle = HAL_InitializeCAN(bus, HAL_CAN_Man_kCTRE,
					pd_module_id, HAL_CAN_Dev_kPowerDistribution, &status);
		} else if (pd_type == HAL_PowerDistributionType_kRev) {
			pd_can_handle = HAL_InitializeCAN(bus, HAL_CAN_Man_kREV,
					pd_module_id, HAL_CAN_Dev_kPowerDistribution, &status);
		}
	}

	pdp_buf->mutate_handle(pd_handle);
	pdp_buf->mutate_type(pd_type);
	pdp_buf->mutate_module_id(pd_module_id);

	if (runtime == HAL_Runtime_Simulation) {
		pdp_buf->mutate_channel_count(24);
	} else if (pd_type == HAL_PowerDistributionType_kCTRE) {
		pdp_buf->mutate_channel_count(16);
	} else if (pd_type == HAL_PowerDistributionType_kRev) {
		pdp_buf->mutate_channel_count(24);
	}
}

void PDPReader::read(schema::PDPData *pdp_buf) {
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
	HAL_GetPowerDistributionAllChannelCurrents(pd_handle, channel_current, 24,
			&status);

	double temperature = HAL_GetPowerDistributionTemperature(pd_handle,
			&status);
	double voltage = HAL_GetPowerDistributionVoltage(pd_handle, &status);
	double total_current = HAL_GetPowerDistributionTotalCurrent(pd_handle,
			&status);
	double total_power = HAL_GetPowerDistributionTotalPower(pd_handle, &status);
	double total_energy = HAL_GetPowerDistributionTotalEnergy(pd_handle,
			&status);

	auto currents = pdp_buf->mutable_channel_current();

	for (int i = 0; i < 24; i++) {
		currents->Mutate(i, channel_current[i]);
	}

	pdp_buf->mutate_temperature(temperature);
	pdp_buf->mutate_voltage(voltage);
	pdp_buf->mutate_total_current(total_current);
	pdp_buf->mutate_total_power(total_power);
	pdp_buf->mutate_total_energy(total_energy);

	pdp_buf->mutate_faults(faults_bits);
	pdp_buf->mutate_sticky_faults(sticky_faults_bits);
}
