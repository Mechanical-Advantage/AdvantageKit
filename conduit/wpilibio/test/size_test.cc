#include <gtest/gtest.h>
#include <hal/HAL.h>

#include "conduit/conduit_schema_generated.h"

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

  ASSERT_EQ(sizeof(HAL_JoystickDescriptor::axisCount),
            sizeof(decltype(joystick.axis_count())));

  ASSERT_EQ(HAL_kMaxJoystickAxes, joystick.axis_types()->size());
  ASSERT_EQ(sizeof(HAL_JoystickDescriptor::axisTypes[0]),
            sizeof(decltype(joystick.axis_types()->Get(0))));

  ASSERT_EQ(HAL_kMaxJoystickAxes, joystick.axis_values()->size());
  ASSERT_EQ(sizeof(HAL_JoystickAxes::axes[0]),
            sizeof(decltype(joystick.axis_values()->Get(0))));

  ASSERT_EQ(sizeof(HAL_JoystickDescriptor::buttonCount),
            sizeof(decltype(joystick.button_count())));

  ASSERT_EQ(sizeof(HAL_JoystickButtons::buttons),
            sizeof(decltype(joystick.buttons())));

  ASSERT_EQ(sizeof(HAL_JoystickDescriptor::povCount),
            sizeof(decltype(joystick.pov_count())));

  ASSERT_EQ(HAL_kMaxJoystickPOVs, joystick.pov_values()->size());

  ASSERT_EQ(sizeof(HAL_JoystickPOVs::povs[0]),
            sizeof(decltype(joystick.pov_values()->Get(0))));

  ASSERT_EQ(sizeof(HAL_JoystickDescriptor::isXbox),
            sizeof(decltype(joystick.is_xbox())));
}