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
#include <hal/IMU.h>
#include <hal/IMUTypes.h>
#include <hal/Ports.h>
#include <hal/Power.h>
#include <hal/PowerDistribution.h>
#include <hal/SystemServer.h>
#include <networktables/NetworkTableInstance.h>
#include <wpi/StackTrace.h>
#include <wpi/timestamp.h>

#include <chrono>
#include <cstdint>
#include <cstring>
#include <iostream>
#include <mutex>

using namespace std::chrono_literals;

void SystemReader::start() {
	const auto inst = nt::NetworkTableInstance { HAL_GetSystemServerHandle() };
	const auto sys_table = inst.GetTable("sys");
	const auto imu_table = inst.GetTable("imu");
	const auto diagnostics_table = inst.GetTable("diagnostics");

	watchdog_active_sub = inst.GetBooleanTopic(
			"/Netcomm/Control/WatchdogActive").Subscribe(false);
	io_frequency_sub = sys_table->GetIntegerTopic("iofreq").Subscribe(0);
	team_number_sub = sys_table->GetIntegerTopic("teamnum").Subscribe(-1);

	const auto network_default = std::vector<double>(10, 0.0);
	network_ethernet_sub =
			diagnostics_table->GetDoubleArrayTopic("eth0").Subscribe(
					network_default);
	network_wifi_sub =
			diagnostics_table->GetDoubleArrayTopic("wlan0").Subscribe(
					network_default);
	network_usb0_sub = diagnostics_table->GetDoubleArrayTopic("usb0").Subscribe(
			network_default);
	network_usb1_sub = diagnostics_table->GetDoubleArrayTopic("usb1").Subscribe(
			network_default);
	for (int i = 0; i < NUM_CAN_BUSES; i++) {
		network_can_subs[i] = diagnostics_table->GetDoubleArrayTopic(
				"can_s" + std::to_string(i)).Subscribe(network_default);
	}

	cpu_percent_sub = sys_table->GetDoubleTopic("cpu").Subscribe(0.0);
	cpu_temp_sub = sys_table->GetDoubleTopic("temp").Subscribe(0.0);

	memory_usage_bytes_sub = sys_table->GetIntegerTopic("ram").Subscribe(0);
	memory_total_bytes_sub = sys_table->GetIntegerTopic("ramtotal").Subscribe(
			0);
	memory_percent_sub = sys_table->GetDoubleTopic("ramutil").Subscribe(0);

	storage_usage_bytes_sub = sys_table->GetIntegerTopic("storage").Subscribe(
			0);
	storage_total_bytes_sub =
			sys_table->GetIntegerTopic("storagetotal").Subscribe(0);
	storage_percent_sub = sys_table->GetDoubleTopic("storageutil").Subscribe(0);

	imu_raw_accel_sub = imu_table->GetDoubleArrayTopic("rawaccel").Subscribe(
			std::vector { 0.0, 0.0, 0.0 });
	imu_raw_gyro_sub = imu_table->GetDoubleArrayTopic("rawgyro").Subscribe(
			std::vector { 0.0, 0.0, 0.0 });
	imu_quaternion_sub = imu_table->GetDoubleArrayTopic("quat").Subscribe(
			std::vector { 1.0, 0.0, 0.0, 0.0 });
	imu_yaw_flat_sub = imu_table->GetDoubleTopic("yaw_flat").Subscribe(0.0);
	imu_yaw_landscape_sub =
			imu_table->GetDoubleTopic("yaw_landscape").Subscribe(0.0);
	imu_yaw_portrait_sub = imu_table->GetDoubleTopic("yaw_portrait").Subscribe(
			0.0);
}

void SystemReader::update_network_status(
		org::littletonrobotics::conduit::schema::NetworkStatus &status,
		std::vector<double> values) {
	status.mutable_rx().mutate_bytes(values[0]);
	status.mutable_tx().mutate_bytes(values[1]);
	status.mutable_rx().mutate_packets(values[2]);
	status.mutable_tx().mutate_packets(values[3]);
	status.mutable_rx().mutate_errors(values[4]);
	status.mutable_tx().mutate_errors(values[5]);
	status.mutable_rx().mutate_dropped(values[6]);
	status.mutable_tx().mutate_dropped(values[7]);
	status.mutable_rx().mutate_bandwidth_kbps(values[8]);
	status.mutable_tx().mutate_bandwidth_kbps(values[9]);
}

void SystemReader::read(schema::SystemData *system_buf) {
	std::int32_t status;
	int64_t timestamp;

	system_buf->mutate_battery_voltage(HAL_GetVinVoltage(&status));
	system_buf->mutate_watchdog_active(watchdog_active_sub.Get());
	system_buf->mutate_io_frequency(io_frequency_sub.Get());
	system_buf->mutate_team_number(team_number_sub.Get());
	system_buf->mutate_epoch_time(wpi::GetSystemTime());

	update_network_status(system_buf->mutable_network_ethernet(),
			network_ethernet_sub.Get());
	update_network_status(system_buf->mutable_network_wifi(),
			network_wifi_sub.Get());
	auto usb0_status = network_usb0_sub.Get();
	auto usb1_status = network_usb1_sub.Get();
	auto usb_status = std::vector<double>(10, 0.0);
	for (size_t i = 0; i < std::min(usb0_status.size(), usb1_status.size());
			i++) {
		usb_status[i] = usb0_status[i] + usb1_status[i];
	}
	update_network_status(system_buf->mutable_network_usb_tether(), usb_status);
	for (int i = 0; i < NUM_CAN_BUSES; i++) {
		update_network_status(system_buf->mutable_network_can()->data()[i],
				network_can_subs[i].Get());
	}

	system_buf->mutate_cpu_percent(cpu_percent_sub.Get());
	system_buf->mutate_cpu_temp(cpu_temp_sub.Get());

	system_buf->mutate_memory_usage_bytes(memory_usage_bytes_sub.Get());
	system_buf->mutate_memory_total_bytes(memory_total_bytes_sub.Get());
	system_buf->mutate_memory_percent(memory_percent_sub.Get());

	system_buf->mutate_storage_usage_bytes(storage_usage_bytes_sub.Get());
	system_buf->mutate_storage_total_bytes(storage_total_bytes_sub.Get());
	system_buf->mutate_storage_percent(storage_percent_sub.Get());

	HAL_Acceleration3d accel;
	HAL_GetIMUAcceleration(&accel, &status);
	system_buf->mutable_imu_accel_raw().mutate_x(accel.x);
	system_buf->mutable_imu_accel_raw().mutate_y(accel.y);
	system_buf->mutable_imu_accel_raw().mutate_z(accel.z);

	HAL_GyroRate3d gyro_rates;
	HAL_GetIMUGyroRates(&gyro_rates, &status);
	system_buf->mutable_imu_gyro_rates().mutate_x(gyro_rates.x);
	system_buf->mutable_imu_gyro_rates().mutate_y(gyro_rates.y);
	system_buf->mutable_imu_gyro_rates().mutate_z(gyro_rates.z);

	HAL_EulerAngles3d gyro_euler;
	HAL_GetIMUEulerAngles(&gyro_euler, &status);
	system_buf->mutable_imu_gyro_euler().mutate_x(gyro_euler.x);
	system_buf->mutable_imu_gyro_euler().mutate_y(gyro_euler.y);
	system_buf->mutable_imu_gyro_euler().mutate_z(gyro_euler.z);

	HAL_Quaternion gyro_quaternion;
	HAL_GetIMUQuaternion(&gyro_quaternion, &status);
	system_buf->mutable_imu_gyro_quaternion().mutate_w(gyro_quaternion.w);
	system_buf->mutable_imu_gyro_quaternion().mutate_x(gyro_quaternion.x);
	system_buf->mutable_imu_gyro_quaternion().mutate_y(gyro_quaternion.y);
	system_buf->mutable_imu_gyro_quaternion().mutate_z(gyro_quaternion.z);

	system_buf->mutate_imu_gyro_yaw_flat(HAL_GetIMUYawFlat(&timestamp));
	system_buf->mutate_imu_gyro_yaw_landscape(
			HAL_GetIMUYawLandscape(&timestamp));
	system_buf->mutate_imu_gyro_yaw_portrait(HAL_GetIMUYawPortrait(&timestamp));
}
