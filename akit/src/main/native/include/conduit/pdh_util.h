// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include "conduit/rev/PDHFrames.h"

#define PDH_STATUS_0_API_ID ((PDH_STATUS_0_FRAME_ID >> 6) & 0x3FF)
#define PDH_STATUS_1_API_ID ((PDH_STATUS_1_FRAME_ID >> 6) & 0x3FF)
#define PDH_STATUS_2_API_ID ((PDH_STATUS_2_FRAME_ID >> 6) & 0x3FF)
#define PDH_STATUS_3_API_ID ((PDH_STATUS_3_FRAME_ID >> 6) & 0x3FF)
#define PDH_STATUS_4_API_ID ((PDH_STATUS_4_FRAME_ID >> 6) & 0x3FF)
