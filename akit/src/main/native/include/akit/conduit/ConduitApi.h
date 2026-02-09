// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once
#include <hal/PowerDistribution.h>
#include "conduit/wpilibio.h"
#include "conduit_schema_generated.h"

namespace akit {

namespace conduit {

class ConduitApi {
	static constexpr int NUM_JOYSTICKS = 6;
	static constexpr int NUM_JOYSTICK_AXES = 12;
	static constexpr int NUM_JOYSTICK_POVS = 12;

public:
	static ConduitApi& getInstance();
	void captureData() {
		wpilibio::capture_data();
	}

	long getTimestamp() {
		return inputs.timestamp();
	}

	long getAllianceStation() {
		return inputs.ds().alliance_station();
	}

	std::string getEventName() {
		return std::string {
				reinterpret_cast<const char*>(inputs.ds().event_name()->data()) };
	}

	std::string getGameSpecificMessage() {
		return std::string {
				reinterpret_cast<const char*>(inputs.ds().game_specific_message()->data()) };
	}

	long getGameSpecificMessageSize() {
		return inputs.ds().game_specific_message_size();
	}

	long getMatchNumber() {
		return inputs.ds().match_number();
	}

	long getReplayNumber() {
		return inputs.ds().replay_number();
	}

	long getMatchType() {
		return inputs.ds().match_type();
	}

	long getControlWord() {
		return inputs.ds().control_word();
	}

	double getMatchTime() {
		return inputs.ds().match_time();
	}

	std::string getJoystickName(int id) {
		return std::string {
				reinterpret_cast<const char*>(inputs.ds().joysticks()->Get(id)->name()) };
	}

	long getJoystickType(int id) {
		return inputs.ds().joysticks()->Get(id)->type();
	}

	long getButtonCount(int id) {
		return inputs.ds().joysticks()->Get(id)->button_count();
	}

	long getButtonValues(int id) {
		return inputs.ds().joysticks()->Get(id)->buttons();
	}

	long getAxisCount(int id) {
		return inputs.ds().joysticks()->Get(id)->axis_count();
	}

	std::array<long, NUM_JOYSTICK_AXES> getAxisTypes(int id) {
		std::array < long, NUM_JOYSTICK_AXES > types;
		auto rawTypes = inputs.ds().joysticks()->Get(id)->axis_types();
		std::copy_n(rawTypes->begin(), NUM_JOYSTICK_AXES, types.begin());
		return types;
	}

	std::array<float, NUM_JOYSTICK_AXES> getAxisValues(int id) {
		std::array<float, NUM_JOYSTICK_AXES> values;
		auto rawValues = inputs.ds().joysticks()->Get(id)->axis_values();
		std::copy_n(rawValues->begin(), NUM_JOYSTICK_AXES, values.begin());
		return values;
	}

	long getPovCount(int id) {
		return inputs.ds().joysticks()->Get(id)->pov_count();
	}

	std::array<long, NUM_JOYSTICK_POVS> getPovValues(int id) {
		std::array < long, NUM_JOYSTICK_POVS > values;
		auto rawValues = inputs.ds().joysticks()->Get(id)->pov_values();
		std::copy_n(rawValues->begin(), NUM_JOYSTICK_POVS, values.begin());
		return values;
	}

	bool isXbox(int id) {
		return inputs.ds().joysticks()->Get(id)->is_xbox();
	}

	double getPDPTemperature() {
		return inputs.pdp().temperature();
	}

	double getPDPVoltage() {
		return inputs.pdp().voltage();
	}

	double getPDPChannelCurrent(int channel) {
		return inputs.pdp().channel_current()->Get(channel);
	}

	double getPDPTotalCurrent() {
		return inputs.pdp().total_current();
	}

	double getPDPTotalEnergy() {
		return inputs.pdp().total_energy();
	}

	double getPDPTotalPower() {
		return inputs.pdp().total_power();
	}

	double getFPGAVersion() {
		return inputs.sys().fpga_version();
	}

	double getFPGARevision() {
		return inputs.sys().fpga_revision();
	}

	std::string getSerialNumber() {
		return std::string {
				reinterpret_cast<const char*>(inputs.sys().serial_number()->data()) };
	}

	std::string getComments() {
		return std::string {
				reinterpret_cast<const char*>(inputs.sys().comments()->data()) };
	}

	long getTeamNumber() {
		return inputs.sys().team_number();
	}

	bool getFPGAButton() {
		return inputs.sys().fpga_button() != 0;
	}

	bool getSystemActive() {
		return inputs.sys().system_active() != 0;
	}

	bool getBrownedOut() {
		return inputs.sys().browned_out() != 0;
	}

	long getCommsDisableCount() {
		return inputs.sys().comms_disable_count();
	}

	bool getRSLState() {
		return inputs.sys().rsl_state() != 0;
	}

	bool getSystemTimeValid() {
		return inputs.sys().system_time_valid() != 0;
	}

	double getVoltageVin() {
		return inputs.sys().voltage_vin();
	}

	double getCurrentVin() {
		return inputs.sys().current_vin();
	}

	double getUserVoltage3v3() {
		return inputs.sys().user_voltage_3v3();
	}

	double getUserCurrent3v3() {
		return inputs.sys().user_current_3v3();
	}

	bool getUserActive3v3() {
		return inputs.sys().user_active_3v3() != 0;
	}

	long getUserCurrentFaults3v3() {
		return inputs.sys().user_current_faults_3v3();
	}

	double getUserVoltage5v() {
		return inputs.sys().user_voltage_5v();
	}

	double getUserCurrent5v() {
		return inputs.sys().user_current_5v();
	}

	bool getUserActive5v() {
		return inputs.sys().user_active_5v() != 0;
	}

	long getUserCurrentFaults5v() {
		return inputs.sys().user_current_faults_5v();
	}

	double getUserVoltage6v() {
		return inputs.sys().user_voltage_6v();
	}

	double getUserCurrent6v() {
		return inputs.sys().user_current_6v();
	}

	bool getUserActive6v() {
		return inputs.sys().user_active_6v() != 0;
	}

	long getUserCurrentFaults6v() {
		return inputs.sys().user_current_faults_6v();
	}

	double getBrownoutVoltage() {
		return inputs.sys().brownout_voltage();
	}

	double getCPUTemp() {
		return inputs.sys().cpu_temp();
	}

	float getCANBusUtilization() {
		return inputs.sys().can_status().percent_bus_utilization();
	}

	long getBusOffCount() {
		return inputs.sys().can_status().bus_off_count();
	}

	long getTxFullCount() {
		return inputs.sys().can_status().tx_full_count();
	}

	long getReceiveErrorCount() {
		return inputs.sys().can_status().receive_error_count();
	}

	long getTransmitErrorCount() {
		return inputs.sys().can_status().transmit_error_count();
	}

	long getEpochTime() {
		return inputs.sys().epoch_time();
	}

	void configurePowerDistribution(int moduleID,
			HAL_PowerDistributionType type) {
		// FIXME: Unimplemented
	}

	long getPDPChannelCount() {
		return inputs.pdp().channel_count();
	}

	long getPDPHandle() {
		return inputs.pdp().handle();
	}

	long getPDPType() {
		return inputs.pdp().type();
	}

	long getPDPModuleId() {
		return inputs.pdp().module_id();
	}

	long getPDPFaults() {
		return inputs.pdp().faults();
	}

	long getPDPStickyFaults() {
		return inputs.pdp().sticky_faults();
	}

private:
	ConduitApi() : inputs { getCoreInputs() } {
	}

	static org::littletonrobotics::conduit::schema::CoreInputs& getCoreInputs() {
		wpilibio::start();
		if (wpilibio::shared_buf == 0)
			wpilibio::make_buffer();
		return *static_cast<org::littletonrobotics::conduit::schema::CoreInputs*>(wpilibio::shared_buf);
	}

	org::littletonrobotics::conduit::schema::CoreInputs &inputs;
};

}

}
