#include "conduit/wpilibio/include/ds_reader.h"

#include <hal/DriverStation.h>
#include <hal/HALBase.h>

#include <cstdint>
#include <cstring>
#include <mutex>

static const int NUM_JOYSTICKS = 6;

void DsReader::start() {
  ds_thread = std::thread(&DsReader::update_ds_data, this);
}

void DsReader::update_ds_data() {
  while (true) {
    HAL_WaitForDSData();

    std::int32_t status;

    uint8_t alliance_station = HAL_GetAllianceStation(&status);

    HAL_MatchInfo match_info;
    HAL_GetMatchInfo(&match_info);

    int32_t control_word;
    HAL_GetControlWord((HAL_ControlWord*)&control_word);

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
      stick_buf->mutate_axis_count(jd.axisCount);

      std::memcpy(stick_buf->mutable_axis_types()->Data(), jd.axisTypes,
                  stick_buf->mutable_axis_types()->size() * sizeof(uint8_t));
      stick_buf->mutate_button_count(jd.buttonCount);
      stick_buf->mutate_pov_count(jd.povCount);
      stick_buf->mutate_is_xbox(jd.isXbox);

      // Read joystick values
      HAL_JoystickAxes axes;
      HAL_GetJoystickAxes(joystickNum, &axes);
      std::memcpy(stick_buf->mutable_axis_values()->Data(), axes.axes,
                  stick_buf->axis_values()->size() * sizeof(float));

      HAL_JoystickPOVs povs;
      HAL_GetJoystickPOVs(joystickNum, &povs);
      std::memcpy(stick_buf->mutable_pov_values()->Data(), povs.povs,
                  stick_buf->pov_values()->size() * sizeof(int16_t));

      HAL_JoystickButtons buttons;
      HAL_GetJoystickButtons(joystickNum, &buttons);
      stick_buf->mutate_buttons(buttons.buttons);
    }

    // Copy all data into the internal buffer
    copy_mutex.lock();
    internal_buf.mutate_alliance_station(alliance_station);
    internal_buf.mutate_match_number(match_info.matchNumber);
    internal_buf.mutate_replay_number(match_info.replayNumber);
    internal_buf.mutate_match_type(match_info.matchType);
    internal_buf.mutate_control_word(control_word);
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
  copy_mutex.lock();
  std::memcpy(ds_buf, &internal_buf, sizeof(schema::DSData));
  copy_mutex.unlock();
}