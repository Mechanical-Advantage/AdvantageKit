// Copyright 2021-2025 FRC 6328
// http://github.com/Mechanical-Advantage
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// version 3 as published by the Free Software Foundation or
// available in the root directory of this project.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.

#include "conduit/ds_reader.h"

#include <hal/DriverStation.h>
#include <hal/HALBase.h>

#include <chrono>
#include <cstdint>
#include <cstring>
#include <iostream>
#include <mutex>
#include <thread>

static const int NUM_JOYSTICKS = 6;

void DsReader::read(schema::DSData* ds_buf) {
  std::int32_t status;

  int32_t control_word;
  HAL_GetControlWord((HAL_ControlWord*)&control_word);

  uint8_t alliance_station = HAL_GetAllianceStation(&status);

  HAL_MatchInfo match_info;
  HAL_GetMatchInfo(&match_info);

  double match_time = HAL_GetMatchTime(&status);

  schema::Joystick stick_bufs[NUM_JOYSTICKS];

  for (int joystickNum = 0; joystickNum < ds_buf->joysticks()->size();
       joystickNum++) {
    HAL_JoystickDescriptor jd;
    HAL_GetJoystickDescriptor(joystickNum, &jd);

    schema::Joystick* stick_buf = &stick_bufs[joystickNum];

    std::memcpy(stick_buf->mutable_name()->Data(), jd.name,
                stick_buf->name()->size());
    stick_buf->mutate_type(jd.type);

    std::memcpy(stick_buf->mutable_axis_types()->Data(), jd.axisTypes,
                stick_buf->mutable_axis_types()->size() * sizeof(uint8_t));
    stick_buf->mutate_is_xbox(jd.isXbox);

    // Read joystick values
    HAL_JoystickAxes axes;
    HAL_GetJoystickAxes(joystickNum, &axes);
    std::memcpy(stick_buf->mutable_axis_values()->Data(), axes.axes,
                stick_buf->axis_values()->size() * sizeof(float));
    stick_buf->mutate_axis_count(axes.count);

    HAL_JoystickPOVs povs;
    HAL_GetJoystickPOVs(joystickNum, &povs);
    std::memcpy(stick_buf->mutable_pov_values()->Data(), povs.povs,
                stick_buf->pov_values()->size() * sizeof(int16_t));
    stick_buf->mutate_pov_count(povs.count);

    HAL_JoystickButtons buttons;
    HAL_GetJoystickButtons(joystickNum, &buttons);
    stick_buf->mutate_buttons(buttons.buttons);
    stick_buf->mutate_button_count(buttons.count);
  }

  // Copy all data into the internal buffer
  ds_buf->mutate_control_word(control_word);
  ds_buf->mutate_alliance_station(alliance_station);
  ds_buf->mutate_match_number(match_info.matchNumber);
  ds_buf->mutate_replay_number(match_info.replayNumber);
  ds_buf->mutate_match_type(match_info.matchType);
  ds_buf->mutate_match_time(match_time);

  ds_buf->mutate_game_specific_message_size(match_info.gameSpecificMessageSize);
  std::memcpy(ds_buf->mutable_game_specific_message()->Data(),
              match_info.gameSpecificMessage,
              ds_buf->game_specific_message()->size());

  std::memcpy(ds_buf->mutable_event_name()->Data(), match_info.eventName,
              ds_buf->event_name()->size());

  std::memcpy(ds_buf->mutable_joysticks()->Data(), stick_bufs,
              ds_buf->joysticks()->size() * sizeof(schema::Joystick));
}