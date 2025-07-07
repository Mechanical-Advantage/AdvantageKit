// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once

#include <networktables/BooleanTopic.h>
#include <networktables/DoubleArrayTopic.h>
#include <networktables/DoubleTopic.h>
#include <networktables/IntegerTopic.h>

#include "conduit_schema_generated.h"
using namespace org::littletonrobotics::conduit;

// Reads system stats data from the RIO. The data is read synchronously as
// there are no periodic updates (unlike DS and PD data).
class SystemReader {
public:
	void start();
	void read(schema::SystemData *system_buf);

private:
	static constexpr int NUM_CAN_BUSES = 5;

	void update_network_status(
			org::littletonrobotics::conduit::schema::NetworkStatus &status,
			std::vector<double> values);

	nt::BooleanSubscriber watchdog_active_sub;
	nt::IntegerSubscriber io_frequency_sub;
	nt::IntegerSubscriber team_number_sub;

	nt::DoubleArraySubscriber network_ethernet_sub;
	nt::DoubleArraySubscriber network_wifi_sub;
	nt::DoubleArraySubscriber network_usb0_sub;
	nt::DoubleArraySubscriber network_usb1_sub;
	nt::DoubleArraySubscriber network_can_subs[NUM_CAN_BUSES];

	nt::DoubleSubscriber cpu_percent_sub;
	nt::DoubleSubscriber cpu_temp_sub;

	nt::IntegerSubscriber memory_usage_bytes_sub;
	nt::IntegerSubscriber memory_total_bytes_sub;
	nt::DoubleSubscriber memory_percent_sub;

	nt::IntegerSubscriber storage_usage_bytes_sub;
	nt::IntegerSubscriber storage_total_bytes_sub;
	nt::DoubleSubscriber storage_percent_sub;

	nt::DoubleArraySubscriber imu_euler_flat_sub;
	nt::DoubleArraySubscriber imu_euler_landscape_sub;
	nt::DoubleArraySubscriber imu_euler_portrait_sub;
};
