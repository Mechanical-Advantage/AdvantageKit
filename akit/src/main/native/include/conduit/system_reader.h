// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once

#include <wpi/nt/BooleanTopic.hpp>
#include <wpi/nt/DoubleArrayTopic.hpp>
#include <wpi/nt/DoubleTopic.hpp>
#include <wpi/nt/IntegerTopic.hpp>

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

	wpi::nt::BooleanSubscriber watchdog_active_sub;
	wpi::nt::IntegerSubscriber io_frequency_sub;
	wpi::nt::IntegerSubscriber team_number_sub;
	wpi::nt::BooleanSubscriber epoch_time_valid_sub;

	wpi::nt::DoubleArraySubscriber network_ethernet_sub;
	wpi::nt::DoubleArraySubscriber network_wifi_sub;
	wpi::nt::DoubleArraySubscriber network_usb0_sub;
	wpi::nt::DoubleArraySubscriber network_usb1_sub;
	wpi::nt::DoubleArraySubscriber network_can_subs[NUM_CAN_BUSES];
	wpi::nt::DoubleArraySubscriber network_can_info_sub;

	wpi::nt::DoubleSubscriber cpu_percent_sub;
	wpi::nt::DoubleSubscriber cpu_temp_sub;

	wpi::nt::IntegerSubscriber memory_usage_bytes_sub;
	wpi::nt::IntegerSubscriber memory_total_bytes_sub;
	wpi::nt::DoubleSubscriber memory_percent_sub;

	wpi::nt::IntegerSubscriber storage_usage_bytes_sub;
	wpi::nt::IntegerSubscriber storage_total_bytes_sub;
	wpi::nt::DoubleSubscriber storage_percent_sub;
};
