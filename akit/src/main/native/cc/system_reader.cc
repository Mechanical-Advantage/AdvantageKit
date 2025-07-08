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
	const auto netcomm_table = inst.GetTable("Netcomm");
	const auto sys_table = inst.GetTable("sys");
	const auto imu_table = inst.GetTable("imu");
	const auto diagnostics_table = inst.GetTable("diagnostics");

	watchdog_active_sub = inst.GetBooleanTopic(
			"/Netcomm/Control/WatchdogActive").Subscribe(false);
	io_frequency_sub = sys_table->GetIntegerTopic("iofreq").Subscribe(0);
	team_number_sub = sys_table->GetIntegerTopic("teamnum").Subscribe(-1);
	epoch_time_valid_sub = netcomm_table->GetBooleanTopic(
			"Control/HasSetWallClock").Subscribe(false);

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
	network_can_info_sub =
			diagnostics_table->GetDoubleArrayTopic("canbusinfo").Subscribe(
					std::vector<double>(5 * NUM_CAN_BUSES, 0.0));

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

	imu_euler_flat_sub = imu_table->GetDoubleArrayTopic("euler_flat").Subscribe(
			std::vector { 0.0, 0.0, 0.0 });
	imu_euler_landscape_sub =
			imu_table->GetDoubleArrayTopic("euler_landscape").Subscribe(
					std::vector { 0.0, 0.0, 0.0 });
	imu_euler_portrait_sub =
			imu_table->GetDoubleArrayTopic("euler_portrait").Subscribe(
					std::vector { 0.0, 0.0, 0.0 });
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
	system_buf->mutate_epoch_time_valid(epoch_time_valid_sub.Get());

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
	auto can_info = network_can_info_sub.Get();
	for (int i = 0; i < NUM_CAN_BUSES; i++) {
		auto &can_info_buf = system_buf->mutable_network_can_info()->data()[i];
		can_info_buf.mutate_max_bandwidth_mbps(can_info[i * 5 + 1]);
		can_info_buf.mutate_is_fd(can_info[i * 5 + 2] > 0);
		can_info_buf.mutate_is_available(can_info[i * 5 + 3] > 0);
		can_info_buf.mutate_is_up(can_info[i * 5 + 4] > 0);
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

	// TODO: Read Euler angles from HAL when API is available

	const auto euler_flat = imu_euler_flat_sub.Get();
	system_buf->mutable_imu_gyro_euler_flat().mutate_x(euler_flat[0]);
	system_buf->mutable_imu_gyro_euler_flat().mutate_y(euler_flat[1]);
	system_buf->mutable_imu_gyro_euler_flat().mutate_z(euler_flat[2]);

	const auto euler_landscape = imu_euler_landscape_sub.Get();
	system_buf->mutable_imu_gyro_euler_landscape().mutate_x(euler_landscape[0]);
	system_buf->mutable_imu_gyro_euler_landscape().mutate_y(euler_landscape[1]);
	system_buf->mutable_imu_gyro_euler_landscape().mutate_z(euler_landscape[2]);

	const auto euler_portrait = imu_euler_portrait_sub.Get();
	system_buf->mutable_imu_gyro_euler_portrait().mutate_x(euler_portrait[0]);
	system_buf->mutable_imu_gyro_euler_portrait().mutate_y(euler_portrait[1]);
	system_buf->mutable_imu_gyro_euler_portrait().mutate_z(euler_portrait[2]);

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
