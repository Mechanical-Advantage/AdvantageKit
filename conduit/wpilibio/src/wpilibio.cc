#include "conduit/wpilibio/include/wpilibio.h"

#include <hal/DriverStation.h>
#include <hal/DriverStationTypes.h>
#include <hal/HALBase.h>
#include <stdlib.h>

#include <cstdint>

#include "conduit/conduit_schema_generated.h"

namespace akit {
namespace conduit {
namespace wpilibio {

using namespace org::littletonrobotics::conduit;

void* shared_buf = 0;
schema::CoreInputs* corein_view;

void make_buffer() {
  // Allocate shared buffer
  shared_buf = malloc(BUF_SIZE);

  // Point view pointers at the buffer at the right offset
  corein_view = reinterpret_cast<schema::CoreInputs*>(shared_buf);
}

void capture_data(void) {
  std::int32_t status;

  corein_view->mutate_timestamp(HAL_GetFPGATime(&status));
  corein_view->mutate_alliance_station(HAL_GetAllianceStation(&status));

  HAL_MatchInfo match_info;
  HAL_GetMatchInfo(&match_info);

  std::memcpy(corein_view->mutable_event_name()->Data(), match_info.eventName,
              corein_view->event_name()->size() * sizeof(uint8_t));
  corein_view->mutate_game_specific_message_size(
      match_info.gameSpecificMessageSize);
  std::memcpy(corein_view->mutable_game_specific_message()->Data(),
              match_info.gameSpecificMessage,
              corein_view->game_specific_message()->size() * sizeof(uint8_t));
  corein_view->mutate_match_number(match_info.matchNumber);
  corein_view->mutate_replay_number(match_info.replayNumber);
  corein_view->mutate_match_type(match_info.matchType);

  HAL_ControlWord control_word;
  int32_t control_word_native;
  HAL_GetControlWord(&control_word);
  std::memcpy(
      &control_word_native, &control_word,
      sizeof(HAL_ControlWord));  // Ugly but it's what WPILib does for this
  corein_view->mutate_control_word(control_word_native);

  corein_view->mutate_match_time(HAL_GetMatchTime(&status));

  for (int joystickNum = 0; joystickNum < corein_view->joysticks()->size();
       joystickNum++) {
    HAL_JoystickDescriptor jd;
    HAL_GetJoystickDescriptor(joystickNum, &jd);

    schema::Joystick* stick_buf =
        corein_view->mutable_joysticks()->GetMutablePointer(joystickNum);

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
}

}  // namespace wpilibio
}  // namespace conduit
}  // namespace akit