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

#include "conduit/pdp_util.h"

void parseStatusFrame1(PdpStatus1 status1, PdpStatus1Result& result) {
  uint16_t raw;
  raw = status1.bits.chan1_h8;
  raw <<= 2;
  raw |= status1.bits.chan1_l2;
  result.chan1 = raw;

  raw = status1.bits.chan2_h6;
  raw <<= 4;
  raw |= status1.bits.chan2_l4;
  result.chan2 = raw;

  raw = status1.bits.chan3_h4;
  raw <<= 6;
  raw |= status1.bits.chan3_l6;
  result.chan3 = raw;

  raw = status1.bits.chan4_h2;
  raw <<= 8;
  raw |= status1.bits.chan4_l8;
  result.chan4 = raw;

  raw = status1.bits.chan5_h8;
  raw <<= 2;
  raw |= status1.bits.chan5_l2;
  result.chan5 = raw;

  raw = status1.bits.chan6_h6;
  raw <<= 4;
  raw |= status1.bits.chan6_l4;
  result.chan6 = raw;
}

void parseStatusFrame2(PdpStatus2 status2, PdpStatus2Result& result) {
  uint16_t raw;
  raw = status2.bits.chan7_h8;
  raw <<= 2;
  raw |= status2.bits.chan7_l2;
  result.chan7 = raw;

  raw = status2.bits.chan8_h6;
  raw <<= 4;
  raw |= status2.bits.chan8_l4;
  result.chan8 = raw;

  raw = status2.bits.chan9_h4;
  raw <<= 6;
  raw |= status2.bits.chan9_l6;
  result.chan9 = raw;

  raw = status2.bits.chan10_h2;
  raw <<= 8;
  raw |= status2.bits.chan10_l8;
  result.chan10 = raw;

  raw = status2.bits.chan11_h8;
  raw <<= 2;
  raw |= status2.bits.chan11_l2;
  result.chan11 = raw;

  raw = status2.bits.chan12_h6;
  raw <<= 4;
  raw |= status2.bits.chan12_l4;
  result.chan12 = raw;
}

void parseStatusFrame3(PdpStatus3 status3, PdpStatus3Result& result) {
  uint16_t raw;
  raw = status3.bits.chan13_h8;
  raw <<= 2;
  raw |= status3.bits.chan13_l2;
  result.chan13 = raw;

  raw = status3.bits.chan14_h6;
  raw <<= 4;
  raw |= status3.bits.chan14_l4;
  result.chan14 = raw;

  raw = status3.bits.chan15_h4;
  raw <<= 6;
  raw |= status3.bits.chan15_l6;
  result.chan15 = raw;

  raw = status3.bits.chan16_h2;
  raw <<= 8;
  raw |= status3.bits.chan16_l8;
  result.chan16 = raw;

  result.internalResBattery_mOhms = status3.bits.internalResBattery_mOhms;
  result.busVoltage = status3.bits.busVoltage;
  result.temp = status3.bits.temp;
}

void parseStatusFrameEnergy(PdpStatusEnergy energy,
                            PdpStatusEnergyResult& result) {
  uint16_t total_current;
  total_current = energy.bits.TotalCurrent_125mAperunit_h8;
  total_current <<= 4;
  total_current |= energy.bits.TotalCurrent_125mAperunit_l4;

  uint16_t total_power;
  total_power = energy.bits.Power_125mWperunit_h4;
  total_power <<= 8;
  total_power |= energy.bits.Power_125mWperunit_m8;
  total_power <<= 4;
  total_power |= energy.bits.Power_125mWperunit_l4;

  uint32_t total_energy;
  total_energy = energy.bits.Energy_125mWPerUnitXTmeas_h4;
  total_energy <<= 4;
  total_energy |= energy.bits.Energy_125mWPerUnitXTmeas_mh8;
  total_energy <<= 8;
  total_energy |= energy.bits.Energy_125mWPerUnitXTmeas_ml8;
  total_energy <<= 8;
  total_energy |= energy.bits.Energy_125mWPerUnitXTmeas_l8;

  result.TmeasMs_likelywillbe20ms_ = energy.bits.TmeasMs_likelywillbe20ms_;
  result.totalCurrent = total_current;
  result.totalPower = total_power;
  result.totalEnergy = total_energy;
}