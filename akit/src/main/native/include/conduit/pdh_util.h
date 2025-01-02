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

#include "conduit/rev/PDHFrames.h"

#define PDH_STATUS_0_API_ID ((PDH_STATUS_0_FRAME_ID >> 6) & 0x3FF)
#define PDH_STATUS_1_API_ID ((PDH_STATUS_1_FRAME_ID >> 6) & 0x3FF)
#define PDH_STATUS_2_API_ID ((PDH_STATUS_2_FRAME_ID >> 6) & 0x3FF)
#define PDH_STATUS_3_API_ID ((PDH_STATUS_3_FRAME_ID >> 6) & 0x3FF)
#define PDH_STATUS_4_API_ID ((PDH_STATUS_4_FRAME_ID >> 6) & 0x3FF)