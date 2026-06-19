// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include "conduit/ds_reader.h"

#include <wpi/hal/DriverStation.h>
#include <wpi/hal/HAL.h>

#include <chrono>
#include <cstdint>
#include <cstring>
#include <iostream>
#include <mutex>
#include <thread>

static constexpr int NUM_JOYSTICKS = 6;

void DsReader::read(schema::DSData *ds_buf) {
	std::int32_t status;

	HAL_ControlWord control_word;
	HAL_GetControlWord(&control_word);

	uint8_t alliance_station = HAL_GetAllianceStation(&status);

	HAL_MatchInfo match_info;
	HAL_GetMatchInfo(&match_info);

	double match_time = HAL_GetMatchTime(&status);

	schema::Joystick stick_bufs[NUM_JOYSTICKS];

	for (int joystickNum = 0; joystickNum < ds_buf->joysticks()->size();
			joystickNum++) {
		HAL_JoystickDescriptor jd;
		HAL_GetJoystickDescriptor(joystickNum, &jd);

		schema::Joystick *stick_buf = &stick_bufs[joystickNum];

		std::memcpy(stick_buf->mutable_name()->Data(), jd.name,
				stick_buf->name()->size());
		stick_buf->mutate_type(jd.gamepadType);
		stick_buf->mutate_supported_outputs(jd.supportedOutputs);
		stick_buf->mutate_is_gamepad(jd.isGamepad);

		// Read joystick values
		HAL_JoystickAxes axes;
		HAL_GetJoystickAxes(joystickNum, &axes);
		stick_buf->mutate_axes_available(axes.available);
		std::memcpy(stick_buf->mutable_axis_values()->Data(), axes.axes,
				stick_buf->axis_values()->size() * sizeof(float));
		std::memcpy(stick_buf->mutable_axis_raw()->Data(), axes.raw,
				stick_buf->axis_raw()->size() * sizeof(int16_t));

		HAL_JoystickPOVs povs;
		HAL_GetJoystickPOVs(joystickNum, &povs);
		stick_buf->mutate_povs_available(povs.available);
		std::memcpy(stick_buf->mutable_pov_values()->Data(), povs.povs,
				stick_buf->pov_values()->size() * sizeof(uint8_t));

		HAL_JoystickButtons buttons;
		HAL_GetJoystickButtons(joystickNum, &buttons);
		stick_buf->mutate_buttons_available(buttons.available);
		stick_buf->mutate_buttons(buttons.buttons);

		HAL_JoystickTouchpads touchpads;
		HAL_GetJoystickTouchpads(joystickNum, &touchpads);
		stick_buf->mutate_touchpad_count(touchpads.count);
		for (int t = 0; t < 2; t++) {
			auto &dest_touchpad = stick_buf->mutable_touchpads()->data()[t];
			const auto &src_touchpad = touchpads.touchpads[t];
			dest_touchpad.mutate_finger_count(src_touchpad.count);
			for (int f = 0; f < 2; f++) {
				auto &dest_finger = dest_touchpad.mutable_fingers()->data()[f];
				const auto &src_finger = src_touchpad.fingers[f];
				dest_finger.mutate_down(src_finger.down);
				dest_finger.mutate_x(src_finger.x);
				dest_finger.mutate_y(src_finger.y);
			}
		}
	}

	// Copy all data into the internal buffer
	ds_buf->mutate_control_word(control_word.value);
	ds_buf->mutate_alliance_station(alliance_station);
	ds_buf->mutate_match_number(match_info.matchNumber);
	ds_buf->mutate_replay_number(match_info.replayNumber);
	ds_buf->mutate_match_type(match_info.matchType);
	ds_buf->mutate_match_time(match_time);

	HAL_GameData game_data;
	HAL_GetGameData(&game_data);
	std::memcpy(ds_buf->mutable_game_data()->Data(), game_data.gameData,
			ds_buf->game_data()->size());

	std::memcpy(ds_buf->mutable_event_name()->Data(), match_info.eventName,
			ds_buf->event_name()->size());

	std::memcpy(ds_buf->mutable_joysticks()->Data(), stick_bufs,
			ds_buf->joysticks()->size() * sizeof(schema::Joystick));
}
