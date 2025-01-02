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

#include <cstdint>

#define PDP_STATUS_1 0x50
#define PDP_STATUS_2 0x51
#define PDP_STATUS_3 0x52
#define PDP_STATUS_ENERGY 0x5D

/* encoder/decoders */
union PdpStatus1 {
  uint8_t data[8];
  struct Bits {
    unsigned chan1_h8 : 8;
    unsigned chan2_h6 : 6;
    unsigned chan1_l2 : 2;
    unsigned chan3_h4 : 4;
    unsigned chan2_l4 : 4;
    unsigned chan4_h2 : 2;
    unsigned chan3_l6 : 6;
    unsigned chan4_l8 : 8;
    unsigned chan5_h8 : 8;
    unsigned chan6_h6 : 6;
    unsigned chan5_l2 : 2;
    unsigned reserved4 : 4;
    unsigned chan6_l4 : 4;
  } bits;
};

struct PdpStatus1Result {
  uint16_t chan1;
  uint16_t chan2;
  uint16_t chan3;
  uint16_t chan4;
  uint16_t chan5;
  uint16_t chan6;
};

union PdpStatus2 {
  uint8_t data[8];
  struct Bits {
    unsigned chan7_h8 : 8;
    unsigned chan8_h6 : 6;
    unsigned chan7_l2 : 2;
    unsigned chan9_h4 : 4;
    unsigned chan8_l4 : 4;
    unsigned chan10_h2 : 2;
    unsigned chan9_l6 : 6;
    unsigned chan10_l8 : 8;
    unsigned chan11_h8 : 8;
    unsigned chan12_h6 : 6;
    unsigned chan11_l2 : 2;
    unsigned reserved4 : 4;
    unsigned chan12_l4 : 4;
  } bits;
};

struct PdpStatus2Result {
  uint16_t chan7;
  uint16_t chan8;
  uint16_t chan9;
  uint16_t chan10;
  uint16_t chan11;
  uint16_t chan12;
};

union PdpStatus3 {
  uint8_t data[8];
  struct Bits {
    unsigned chan13_h8 : 8;
    unsigned chan14_h6 : 6;
    unsigned chan13_l2 : 2;
    unsigned chan15_h4 : 4;
    unsigned chan14_l4 : 4;
    unsigned chan16_h2 : 2;
    unsigned chan15_l6 : 6;
    unsigned chan16_l8 : 8;
    unsigned internalResBattery_mOhms : 8;
    unsigned busVoltage : 8;
    unsigned temp : 8;
  } bits;
};

struct PdpStatus3Result {
  uint16_t chan13;
  uint16_t chan14;
  uint16_t chan15;
  uint16_t chan16;
  uint8_t internalResBattery_mOhms;
  uint8_t busVoltage;
  uint8_t temp;
};

union PdpStatusEnergy {
  uint8_t data[8];
  struct Bits {
    unsigned TmeasMs_likelywillbe20ms_ : 8;
    unsigned TotalCurrent_125mAperunit_h8 : 8;
    unsigned Power_125mWperunit_h4 : 4;
    unsigned TotalCurrent_125mAperunit_l4 : 4;
    unsigned Power_125mWperunit_m8 : 8;
    unsigned Energy_125mWPerUnitXTmeas_h4 : 4;
    unsigned Power_125mWperunit_l4 : 4;
    unsigned Energy_125mWPerUnitXTmeas_mh8 : 8;
    unsigned Energy_125mWPerUnitXTmeas_ml8 : 8;
    unsigned Energy_125mWPerUnitXTmeas_l8 : 8;
  } bits;
};

struct PdpStatusEnergyResult {
  uint8_t TmeasMs_likelywillbe20ms_;
  uint16_t totalCurrent;
  uint16_t totalPower;
  uint32_t totalEnergy;
};

void parseStatusFrame1(PdpStatus1 status1, PdpStatus1Result& result);
void parseStatusFrame2(PdpStatus2 status2, PdpStatus2Result& result);
void parseStatusFrame3(PdpStatus3 status3, PdpStatus3Result& result);
void parseStatusFrameEnergy(PdpStatusEnergy energy,
                            PdpStatusEnergyResult& result);