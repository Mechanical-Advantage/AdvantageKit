// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include <catch2/catch_test_macros.hpp>
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

TEST_CASE("DsDataSizes", "[SizeTests]") {
	schema::DSData ds;
	REQUIRE(sizeof(HAL_AllianceStationID) ==
			sizeof(decltype(ds.alliance_station())));

	REQUIRE(sizeof(HAL_MatchInfo::eventName) == ds.event_name()->size());
	REQUIRE(sizeof(HAL_MatchInfo::eventName[0]) ==
			sizeof(decltype(ds.event_name()->Get(0))));
	REQUIRE(sizeof(HAL_GameData) == ds.game_data()->size());

	REQUIRE(sizeof(HAL_MatchInfo::matchNumber) ==
			sizeof(decltype(ds.match_number())));
	REQUIRE(sizeof(HAL_MatchInfo::replayNumber) ==
			sizeof(decltype(ds.replay_number())));
	REQUIRE(sizeof(HAL_MatchInfo::matchType) ==
			sizeof(decltype(ds.match_type())));

	REQUIRE(sizeof(decltype(HAL_GetMatchTime(0))) ==
			sizeof(decltype(ds.match_time())));

	REQUIRE(sizeof(HAL_ControlWord) == sizeof(decltype(ds.control_word())));

	// Ensure joystick count matches
	REQUIRE(HAL_MAX_JOYSTICKS == ds.joysticks()->size());
}

TEST_CASE("JoystickSizes", "[SizeTests]") {
	schema::Joystick joystick;
	REQUIRE(sizeof(HAL_JoystickDescriptor::name) == joystick.name()->size());
	REQUIRE(sizeof(HAL_JoystickDescriptor::name[0]) ==
			sizeof(decltype(joystick.name()->Get(0))));

	REQUIRE(sizeof(HAL_JoystickDescriptor::gamepadType) ==
			sizeof(decltype(joystick.type())));

	REQUIRE(sizeof(HAL_JoystickDescriptor::supportedOutputs) ==
			sizeof(decltype(joystick.supported_outputs())));

	REQUIRE(sizeof(HAL_JoystickAxes::available) ==
			sizeof(decltype(joystick.axes_available())));

	REQUIRE(HAL_MAX_JOYSTICK_AXES == joystick.axis_values()->size());
	REQUIRE(sizeof(HAL_JoystickAxes::axes[0]) ==
			sizeof(decltype(joystick.axis_values()->Get(0))));

	REQUIRE(HAL_MAX_JOYSTICK_AXES == joystick.axis_raw()->size());
	REQUIRE(sizeof(HAL_JoystickAxes::raw[0]) ==
			sizeof(decltype(joystick.axis_raw()->Get(0))));

	REQUIRE(sizeof(HAL_JoystickButtons::available) ==
			sizeof(decltype(joystick.buttons_available())));

	REQUIRE(sizeof(HAL_JoystickButtons::buttons) ==
			sizeof(decltype(joystick.buttons())));

	REQUIRE(sizeof(HAL_JoystickPOVs::available) ==
			sizeof(decltype(joystick.povs_available())));

	REQUIRE(HAL_MAX_JOYSTICK_POVS == joystick.pov_values()->size());

	REQUIRE(sizeof(HAL_JoystickPOVs::povs[0]) ==
			sizeof(decltype(joystick.pov_values()->Get(0))));

	REQUIRE(sizeof(HAL_JoystickDescriptor::isGamepad) ==
			sizeof(decltype(joystick.is_gamepad())));

	REQUIRE(sizeof(HAL_JoystickTouchpads::count) ==
			sizeof(decltype(joystick.touchpad_count())));

	REQUIRE(HAL_MAX_JOYSTICK_TOUCHPADS == joystick.touchpads()->size());
	REQUIRE(sizeof(HAL_JoystickTouchpads::touchpads[0]) ==
			sizeof(decltype(*(joystick.touchpads()->Get(0)))));

	REQUIRE(sizeof(HAL_JoystickTouchpad::count) ==
			sizeof(decltype(joystick.touchpads()->Get(0)->finger_count())));

	REQUIRE(HAL_MAX_JOYSTICK_TOUCHPAD_FINGERS ==
			joystick.touchpads()->Get(0)->fingers()->size());

	REQUIRE(
			sizeof(HAL_JoystickTouchpadFinger) ==
			sizeof(decltype(*(joystick.touchpads()->Get(0)->fingers()->Get(0)))));
}

TEST_CASE("PDPDataSizes", "[SizeTests]") {
	schema::PDPData pdp;

	REQUIRE(sizeof(decltype(HAL_InitializePowerDistribution(
									0, 0, HAL_POWER_DISTRIBUTION_CTRE, 0, 0))) ==
			sizeof(decltype(pdp.handle())));

	REQUIRE(sizeof(decltype(HAL_GetPowerDistributionNumChannels(0, 0))) ==
			sizeof(decltype(pdp.channel_count())));

	REQUIRE(sizeof(decltype(HAL_GetPowerDistributionType(0, 0))) ==
			sizeof(decltype(pdp.type())));

	REQUIRE(sizeof(decltype(HAL_GetPowerDistributionModuleNumber(0, 0))) ==
			sizeof(decltype(pdp.module_id())));

	REQUIRE(sizeof(HAL_PowerDistributionFaults) ==
			sizeof(decltype(pdp.faults())));

	REQUIRE(sizeof(HAL_PowerDistributionStickyFaults) ==
			sizeof(decltype(pdp.sticky_faults())));

	REQUIRE(sizeof(decltype(HAL_GetPowerDistributionTemperature(0, 0))) ==
			sizeof(decltype(pdp.temperature())));

	REQUIRE(sizeof(decltype(HAL_GetPowerDistributionVoltage(0, 0))) ==
			sizeof(decltype(pdp.voltage())));

	REQUIRE(sizeof(decltype(HAL_GetPowerDistributionNumChannels(0, 0))) ==
			sizeof(decltype(pdp.channel_count())));

	REQUIRE(sizeof(decltype(HAL_GetPowerDistributionTotalCurrent(0, 0))) ==
			sizeof(decltype(pdp.total_current())));

	REQUIRE(sizeof(decltype(HAL_GetPowerDistributionTotalPower(0, 0))) ==
			sizeof(decltype(pdp.total_power())));

	REQUIRE(sizeof(decltype(HAL_GetPowerDistributionTotalEnergy(0, 0))) ==
			sizeof(decltype(pdp.total_energy())));
}

TEST_CASE("SysDataSizes", "[SizeTests]") {
	schema::SystemData sys;

	REQUIRE(sizeof(decltype(HAL_GetVinVoltage(0))) ==
			sizeof(decltype(sys.battery_voltage())));

	REQUIRE(sizeof(decltype(WPI_GetSystemTime())) ==
			sizeof(decltype(sys.epoch_time())));
}
