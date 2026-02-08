// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once
#include <array>
#include "conduit/wpilibio.h"
#include "conduit_schema_generated.h"

namespace akit {

namespace conduit {

class ConduitApi {
public:
	static ConduitApi& getInstance();
	void captureData() {
		wpilibio::capture_data();
	}

	int64_t getTimestamp() {
		return inputs.timestamp();
	}

	int32_t getAllianceStation() {
		return inputs.ds().alliance_station();
	}

	std::string getEventName() {
		return std::string { inputs.ds().event_name()->data() };
	}

	std::string getGameSpecificMessage() {
		return std::string { inputs.ds().game_specific_message()->data() };
	}

	uint16_t getGameSpecificMessageSize() {
		return inputs.ds().game_specific_message_size();
	}

	uint16_t getMatchNumber() {
		return inputs.ds().match_number();
	}

	uint8_t getReplayNumber() {
		return inputs.ds().replay_number();
	}

	int32_t getMatchType() {
		return inputs.ds().match_type();
	}

	int32_t getControlWord() {
		return inputs.ds().control_word();
	}

	double getMatchTime() {
		return inputs.ds().match_time();
	}

	std::string getJoystickName(int id) {
		return std::string { inputs.ds().joysticks()->Get(id)->name() };
	}

	uint8_t getJoystickType(int id) {
		return inputs.ds().joysticks()->Get(id)->type();
	}

	uint8_t getButtonCount(int id) {
		return inputs.ds().joysticks()->Get(id)->button_count();
	}

	int32_t getButtonValues(int id) {
		return inputs.ds().joysticks()->Get(id)->buttons();
	}

	int16_t getAxisCount(int id) {
		return inputs.ds().joysticks()->Get(id)->axis_count();
	}

	std::array<uint8_t, NUM_JOYSTICK_AXES> getAxisTypes(int id) {
		auto types = inputs.ds().joysticks()->Get(id)->axis_types();
		return {types->begin(), types->end()};
	}

	std::array<float, NUM_JOYSTICK_AXES> getAxisValues(int id) {
		auto values = inputs.ds().joysticks()->Get(id)->axis_values();
		return (values->begin(), values->end());
	}

	int16_t getPovCount(int id) {
		return inputs.ds().joysticks()->Get(id)->pov_count();
	}

	bool isXbox(int id) {
		return inputs.ds().joysticks()->Get(id).is_xbox();
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

	double getFPGAVersion() {
		return inputs.sys().fpga_version();
	}

	double getFPGARevision() {
		return inputs.sys().fpga_revision();
	}

	std::string getSerialNumber() {
		return std::string { inputs.sys().serial_number().data() };
	}

	std::string getComments() {
		return std::string { inputs.sys().comments().data() };
	}

	int32_t getTeamNumber() {
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

	int32_t getCommsDisableCount() {
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
		return inputs.sys().user_current_3v();
	}

	bool getUserActive3v3() {
		return inputs.sys().user_active_3v3() != 0;
	}

	int32_t getUserCurrentFaults3v3() {
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

	int32_t getUserCurrentFaults5v() {
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

	int32_t getUserCurrentFaults6v() {
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

	uint32_t getBusOffCount() {
		return inputs.sys().can_status().bus_off_count();
	}

	uint32_t getTxFullCount() {
		return inputs.sys().can_status().tx_full_count();
	}

	uint32_t getReceiveErrorCount() {
		return inputs.sys().can_status().receive_error_count();
	}

	uint32_t getTransmitErrorCount() {
		return inputs.sys().can_status().transmit_error_count();
	}

	uint64_t getEpochTime() {
		return inputs.sys().epoch_time();
	}

	void configurePowerDistribution(int moduleID, int type) {
		// FIXME: Unimplemented
	}

	int32_t getPDPChannelCount() {
		return inputs.pdp().channel_count();
	}

	int32_t getPDPHandle() {
		return inputs.pdp().handle();
	}

	int32_t getPDPType() {
		return inputs.pdp().type();
	}

	int32_t getPDPModuleId() {
		return inputs.pdp().module_id();
	}

	uint32_t getPDPFaults() {
		return inputs.pdp().faults();
	}

	uint32_t getPDPStickyFaults() {
		return inputs.pdp().sticky_faults();
	}

private:
	ConduitApi() : inputs { getCoreInputs() } {
	}

	static org::littletonrobotics::conduit::schema::CoreInputs& getCoreInputs() {
		wpilibio::start();
		if (wpilibio::shared_buf == 0)
			wpilibio::make_buffer();
		return *wpilibio::shared_buf;
	}

	static constexpr int NUM_JOYSTICKS = 6;
	static constexpr int NUM_JOYSTICK_AXES = 12;
	static constexpr int NUM_JOYSTICK_POVS = 12;

	org::littletonrobotics::conduit::schema::CoreInputs &inputs;
};

}

}
