// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include <gtest/gtest.h>
#include <wpi/hal/CAN.h>
#include <wpi/hal/DriverStationTypes.h>
#include <wpi/hal/HAL.h>
#include <wpi/hal/Power.h>
#include <wpi/hal/PowerDistribution.h>
#include <wpi/util/timestamp.h>

#include <wpi/hal/DriverStation.hpp>

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
	ASSERT_EQ(sizeof(HAL_GameData), ds.game_data()->size());

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
	ASSERT_EQ(HAL_MAX_JOYSTICKS, ds.joysticks()->size());

	// Ensure OpMode option fields sizes match expected types
	ASSERT_EQ(sizeof(int64_t), sizeof(decltype(ds.op_mode().id())));
	ASSERT_EQ(sizeof(int32_t), sizeof(decltype(ds.op_mode().text_color())));
	ASSERT_EQ(sizeof(int32_t), sizeof(decltype(ds.op_mode().background_color())));
}

TEST(SizeTests, JoystickSizes) {
	schema::Joystick joystick;
	ASSERT_EQ(sizeof(HAL_JoystickDescriptor::name), joystick.name()->size());
	ASSERT_EQ(sizeof(HAL_JoystickDescriptor::name[0]),
			sizeof(decltype(joystick.name()->Get(0))));

	ASSERT_EQ(sizeof(HAL_JoystickDescriptor::gamepadType),
			sizeof(decltype(joystick.type())));

	ASSERT_EQ(sizeof(HAL_JoystickDescriptor::supportedOutputs),
			sizeof(decltype(joystick.supported_outputs())));

	ASSERT_EQ(sizeof(HAL_JoystickAxes::available),
			sizeof(decltype(joystick.axis_count())));

	ASSERT_EQ(HAL_MAX_JOYSTICK_AXES, joystick.axis_values()->size());
	ASSERT_EQ(sizeof(HAL_JoystickAxes::axes[0]),
			sizeof(decltype(joystick.axis_values()->Get(0))));

	ASSERT_EQ(HAL_MAX_JOYSTICK_AXES, joystick.axis_raw()->size());
	ASSERT_EQ(sizeof(HAL_JoystickAxes::raw[0]),
			sizeof(decltype(joystick.axis_raw()->Get(0))));

	ASSERT_EQ(sizeof(HAL_JoystickButtons::available),
			sizeof(decltype(joystick.buttons_available())));

	ASSERT_EQ(sizeof(HAL_JoystickButtons::buttons),
			sizeof(decltype(joystick.buttons())));

	ASSERT_EQ(sizeof(HAL_JoystickPOVs::available),
			sizeof(decltype(joystick.pov_count())));

	ASSERT_EQ(HAL_MAX_JOYSTICK_POVS, joystick.pov_values()->size());

	ASSERT_EQ(sizeof(HAL_JoystickPOVs::povs[0]),
			sizeof(decltype(joystick.pov_values()->Get(0))));

	ASSERT_EQ(sizeof(HAL_JoystickDescriptor::isGamepad),
			sizeof(decltype(joystick.is_gamepad())));

	ASSERT_EQ(sizeof(HAL_JoystickTouchpads::count),
			sizeof(decltype(joystick.touchpad_count())));

	ASSERT_EQ(HAL_MAX_JOYSTICK_TOUCHPADS, joystick.touchpads()->size());
	ASSERT_EQ(sizeof(HAL_JoystickTouchpads::touchpads[0]),
			sizeof(decltype(*(joystick.touchpads()->Get(0)))));

	ASSERT_EQ(sizeof(HAL_JoystickTouchpad::count),
			sizeof(decltype(joystick.touchpads()->Get(0)->finger_count())));

	ASSERT_EQ(HAL_MAX_JOYSTICK_TOUCHPAD_FINGERS,
			joystick.touchpads()->Get(0)->fingers()->size());

	ASSERT_EQ(
			sizeof(HAL_JoystickTouchpadFinger),
			sizeof(decltype(*(joystick.touchpads()->Get(0)->fingers()->Get(0)))));
}

TEST(SizeTests, PDPDataSizes) {
	schema::PDPData pdp;

	ASSERT_EQ(sizeof(decltype(HAL_InitializePowerDistribution(
									0, 0, HAL_POWER_DISTRIBUTION_CTRE, 0, 0))),
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

	ASSERT_EQ(sizeof(decltype(HAL_GetPowerDistributionTemperature(0, 0))),
			sizeof(decltype(pdp.temperature())));

	ASSERT_EQ(sizeof(decltype(HAL_GetPowerDistributionVoltage(0, 0))),
			sizeof(decltype(pdp.voltage())));

	ASSERT_EQ(sizeof(decltype(HAL_GetPowerDistributionNumChannels(0, 0))),
			sizeof(decltype(pdp.channel_count())));

	ASSERT_EQ(sizeof(decltype(HAL_GetPowerDistributionTotalCurrent(0, 0))),
			sizeof(decltype(pdp.total_current())));

	ASSERT_EQ(sizeof(decltype(HAL_GetPowerDistributionTotalPower(0, 0))),
			sizeof(decltype(pdp.total_power())));

	ASSERT_EQ(sizeof(decltype(HAL_GetPowerDistributionTotalEnergy(0, 0))),
			sizeof(decltype(pdp.total_energy())));
}

TEST(SizeTests, SysDataSizes) {
	schema::SystemData sys;

	ASSERT_EQ(sizeof(decltype(HAL_GetVinVoltage(0))),
			sizeof(decltype(sys.battery_voltage())));

	ASSERT_EQ(sizeof(decltype(WPI_GetSystemTime())),
			sizeof(decltype(sys.epoch_time())));
}
