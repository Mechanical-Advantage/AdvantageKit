#include "conduit/wpilibio/include/rev/PDHFrames.h"

#define PDH_STATUS_0_API_ID ((PDH_STATUS_0_FRAME_ID >> 6) & 0x3FF)
#define PDH_STATUS_1_API_ID ((PDH_STATUS_1_FRAME_ID >> 6) & 0x3FF)
#define PDH_STATUS_2_API_ID ((PDH_STATUS_2_FRAME_ID >> 6) & 0x3FF)
#define PDH_STATUS_3_API_ID ((PDH_STATUS_3_FRAME_ID >> 6) & 0x3FF)
#define PDH_STATUS_4_API_ID ((PDH_STATUS_4_FRAME_ID >> 6) & 0x3FF)