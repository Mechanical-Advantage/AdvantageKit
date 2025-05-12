// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include <gtest/gtest.h>
#include <hal/HAL.h>
#include <hal/PowerDistribution.h>
#include <hal/CAN.h>
#include <hal/Power.h>
#include <wpi/timestamp.h>

#include "conduit_schema_generated.h"

using namespace org::littletonrobotics::conduit;

// Tests to ensure the sizes of flatbuffer structures match the sizes of their
// HAL equivalents

TEST(SizeTests, DsDataSizes) {
	schema::DSData ds;
	ASSERT_EQ(sizeof(HAL_AllianceStationID),
			sizeof(decltype(ds.alliance_station())));

	ASSERT_EQ(sizeof(HAL_MatchInfo::eventName), ds.event_name()->size());
	ASSERT_EQ(sizeof(HAL_MatchInfo::eventName[0]),
			sizeof(decltype(ds.event_name()->Get(0))));

	ASSERT_EQ(sizeof(HAL_MatchInfo::gameSpecificMessageSize),
			sizeof(decltype(ds.game_specific_message_size())));

	ASSERT_EQ(sizeof(HAL_MatchInfo::gameSpecificMessage),
			ds.game_specific_message()->size());
	ASSERT_EQ(sizeof(HAL_MatchInfo::gameSpecificMessage[0]),
			sizeof(decltype(ds.game_specific_message()->Get(0))));

	ASSERT_EQ(sizeof(HAL_MatchInfo::matchNumber),
			sizeof(decltype(ds.match_number())));
	ASSERT_EQ(sizeof(HAL_MatchInfo::replayNumber),
			sizeof(decltype(ds.replay_number())));
	ASSERT_EQ(sizeof(HAL_MatchInfo::matchType),
			sizeof(decltype(ds.match_type())));

	ASSERT_EQ(sizeof(decltype(HAL_GetMatchTime(0))),
			sizeof(decltype(ds.match_time())));

	ASSERT_EQ(sizeof(HAL_ControlWord), sizeof(decltype(ds.control_word())));

	// Ensure joystick count matches
	ASSERT_EQ(HAL_kMaxJoysticks, ds.joysticks()->size());
}

TEST(SizeTests, JoystickSizes) {
	schema::Joystick joystick;
	ASSERT_EQ(sizeof(HAL_JoystickDescriptor::name), joystick.name()->size());
	ASSERT_EQ(sizeof(HAL_JoystickDescriptor::name[0]),
			sizeof(decltype(joystick.name()->Get(0))));

	ASSERT_EQ(sizeof(HAL_JoystickDescriptor::type),
			sizeof(decltype(joystick.type())));

	ASSERT_EQ(sizeof(HAL_JoystickAxes::count),
			sizeof(decltype(joystick.axis_count())));

	ASSERT_EQ(HAL_kMaxJoystickAxes, joystick.axis_types()->size());
	ASSERT_EQ(sizeof(HAL_JoystickDescriptor::axisTypes[0]),
			sizeof(decltype(joystick.axis_types()->Get(0))));

	ASSERT_EQ(HAL_kMaxJoystickAxes, joystick.axis_values()->size());
	ASSERT_EQ(sizeof(HAL_JoystickAxes::axes[0]),
			sizeof(decltype(joystick.axis_values()->Get(0))));

	ASSERT_EQ(sizeof(HAL_JoystickButtons::count),
			sizeof(decltype(joystick.button_count())));

	ASSERT_EQ(sizeof(HAL_JoystickButtons::buttons),
			sizeof(decltype(joystick.buttons())));

	ASSERT_EQ(sizeof(HAL_JoystickPOVs::count),
			sizeof(decltype(joystick.pov_count())));

	ASSERT_EQ(HAL_kMaxJoystickPOVs, joystick.pov_values()->size());

	ASSERT_EQ(sizeof(HAL_JoystickPOVs::povs[0]),
			sizeof(decltype(joystick.pov_values()->Get(0))));

	ASSERT_EQ(sizeof(HAL_JoystickDescriptor::isXbox),
			sizeof(decltype(joystick.is_xbox())));
}

TEST(SizeTests, PDPDataSizes) {
	schema::PDPData pdp;

	ASSERT_EQ(sizeof(decltype(HAL_InitializePowerDistribution(0,
									HAL_PowerDistributionType_kCTRE, 0, 0))),
			sizeof(decltype(pdp.handle())));

	ASSERT_EQ(sizeof(decltype(HAL_GetPowerDistributionNumChannels(0, 0))),
			sizeof(decltype(pdp.channel_count())));

	ASSERT_EQ(sizeof(decltype(HAL_GetPowerDistributionType(0, 0))),
			sizeof(decltype(pdp.type())));

	ASSERT_EQ(sizeof(decltype(HAL_GetPowerDistributionModuleNumber(0, 0))),
			sizeof(decltype(pdp.module_id())));

	ASSERT_EQ(sizeof(HAL_PowerDistributionFaults),
			sizeof(decltype(pdp.faults())));

	ASSERT_EQ(sizeof(HAL_PowerDistributionStickyFaults),
			sizeof(decltype(pdp.sticky_faults())));

	ASSERT_EQ(sizeof(decltype(HAL_GetPowerDistributionTemperature(0,0))),
			sizeof(decltype(pdp.temperature())));

	ASSERT_EQ(sizeof(decltype(HAL_GetPowerDistributionVoltage(0,0))),
			sizeof(decltype(pdp.voltage())));

	ASSERT_EQ(sizeof(decltype(HAL_GetPowerDistributionNumChannels(0,0))),
			sizeof(decltype(pdp.channel_count())));

	// Not sure how to test the array size for HAL_GetPowerDistributionAllChannelCurrents

	ASSERT_EQ(sizeof(decltype(HAL_GetPowerDistributionTotalCurrent(0,0))),
			sizeof(decltype(pdp.total_current())));
	ASSERT_EQ(sizeof(decltype(HAL_GetPowerDistributionTotalPower(0,0))),
			sizeof(decltype(pdp.total_power())));
	ASSERT_EQ(sizeof(decltype(HAL_GetPowerDistributionTotalEnergy(0,0))),
			sizeof(decltype(pdp.total_energy())));
}

TEST(SizeTests, SysDataSizes) {
	schema::SystemData sys;

	ASSERT_EQ(sizeof(decltype(HAL_GetVinVoltage(0))),
			sizeof(decltype(sys.voltage_vin())));

	ASSERT_EQ(sizeof(decltype(HAL_GetVinCurrent(0))),
			sizeof(decltype(sys.current_vin())));

	ASSERT_EQ(sizeof(decltype(HAL_GetUserVoltage3V3(0))),
			sizeof(decltype(sys.user_voltage_3v3())));

	ASSERT_EQ(sizeof(decltype(HAL_GetUserCurrent3V3(0))),
			sizeof(decltype(sys.user_current_3v3())));

	ASSERT_EQ(sizeof(decltype(HAL_GetUserActive3V3(0))),
			sizeof(decltype(sys.user_active_3v3())));

	ASSERT_EQ(sizeof(decltype(HAL_GetUserCurrentFaults3V3(0))),
			sizeof(decltype(sys.user_current_faults_3v3())));

	ASSERT_EQ(sizeof(decltype(HAL_GetUserVoltage5V(0))),
			sizeof(decltype(sys.user_voltage_5v())));

	ASSERT_EQ(sizeof(decltype(HAL_GetUserCurrent5V(0))),
			sizeof(decltype(sys.user_current_5v())));

	ASSERT_EQ(sizeof(decltype(HAL_GetUserActive5V(0))),
			sizeof(decltype(sys.user_active_5v())));

	ASSERT_EQ(sizeof(decltype(HAL_GetUserCurrentFaults5V(0))),
			sizeof(decltype(sys.user_current_faults_5v())));

	ASSERT_EQ(sizeof(decltype(HAL_GetUserVoltage6V(0))),
			sizeof(decltype(sys.user_voltage_6v())));

	ASSERT_EQ(sizeof(decltype(HAL_GetUserCurrent6V(0))),
			sizeof(decltype(sys.user_current_6v())));

	ASSERT_EQ(sizeof(decltype(HAL_GetUserActive6V(0))),
			sizeof(decltype(sys.user_active_6v())));

	ASSERT_EQ(sizeof(decltype(HAL_GetUserCurrentFaults6V(0))),
			sizeof(decltype(sys.user_current_faults_6v())));

	ASSERT_EQ(sizeof(decltype(HAL_GetBrownedOut(0))),
			sizeof(decltype(sys.browned_out())));

	ASSERT_EQ(sizeof(decltype(HAL_GetSystemActive(0))),
			sizeof(decltype(sys.system_active())));

	// Not sure how to test size for HAL_CAN_GetCANStatus

	ASSERT_EQ(sizeof(decltype(wpi::GetSystemTime())),
			sizeof(decltype(sys.epoch_time())));

}
