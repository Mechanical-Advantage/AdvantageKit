// Copyright 2021-2024 FRC 6328
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

#include "conduit/wpilibio/include/ds_reader.h"

#include <hal/DriverStation.h>
#include <hal/HALBase.h>

#include <chrono>
#include <cstdint>
#include <cstring>
#include <iostream>
#include <mutex>
#include <thread>

static const int NUM_JOYSTICKS = 6;
using namespace std::chrono_literals;

DsReader::DsReader() : is_running(false) {}

DsReader::~DsReader() {
  if (is_running) {
    is_running = false;  // Stop the thread when we destruct this object
    ds_thread.join();
  }
}

void DsReader::start() {
  ds_thread = std::thread(&DsReader::update_ds_data, this);
}

void DsReader::update_ds_data() {
  is_running = true;
  while (is_running) {
    std::this_thread::sleep_for(20ms);
    HAL_RefreshDSData();
    std::int32_t status;

    int32_t control_word;
    HAL_GetControlWord((HAL_ControlWord*)&control_word);

    uint8_t alliance_station = HAL_GetAllianceStation(&status);

    HAL_MatchInfo match_info;
    HAL_GetMatchInfo(&match_info);

    double match_time = HAL_GetMatchTime(&status);

    schema::Joystick stick_bufs[NUM_JOYSTICKS];

    for (int joystickNum = 0; joystickNum < internal_buf.joysticks()->size();
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
    copy_mutex.lock();
    internal_buf.mutate_control_word(control_word);
    internal_buf.mutate_alliance_station(alliance_station);
    internal_buf.mutate_match_number(match_info.matchNumber);
    internal_buf.mutate_replay_number(match_info.replayNumber);
    internal_buf.mutate_match_type(match_info.matchType);
    internal_buf.mutate_match_time(match_time);

    internal_buf.mutate_game_specific_message_size(
        match_info.gameSpecificMessageSize);
    std::memcpy(internal_buf.mutable_game_specific_message()->Data(),
                match_info.gameSpecificMessage,
                internal_buf.game_specific_message()->size());

    std::memcpy(internal_buf.mutable_event_name()->Data(), match_info.eventName,
                internal_buf.event_name()->size());

    std::memcpy(internal_buf.mutable_joysticks()->Data(), stick_bufs,
                internal_buf.joysticks()->size() * sizeof(schema::Joystick));
    copy_mutex.unlock();
  }
}

void DsReader::read(schema::DSData* ds_buf) {
  if (copy_mutex.try_lock_for(5s)) {
    std::memcpy(ds_buf, &internal_buf, sizeof(schema::DSData));
    copy_mutex.unlock();
  } else {
    std::cout
        << "[conduit] Could not acquire DS read lock after 5 seconds!  Exiting!"
        << std::endl;
    exit(1);
  }
}