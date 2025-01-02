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

#pragma once

#include "conduit_schema_generated.h"
using namespace org::littletonrobotics::conduit;

#include <hal/CANAPITypes.h>
#include <hal/HALBase.h>
#include <hal/PowerDistribution.h>
#include <jni.h>

#include <atomic>
#include <mutex>
#include <thread>

// Reads data from the power distribution panel.  The data is read in a thread
// and copied into a schema::PDPData internal buffer.  This copying is done
// under lock. When a read is requested, the same lock is acquired.  This
// minimizes contention between the costly PDP read functions and the reading of
// data into the buffer. The copy between the internal buffer and the external
// buffer is a single copy operation since the two buffers are the same
// structure
class PDPReader {
 public:
  void read(schema::PDPData* pdp_buf);
  void configure(JNIEnv* env, jint module, jint type, schema::PDPData* pdp_buf);

 private:
  void update_ctre_pdp_data(schema::PDPData* pdp_buf);
  void update_rev_pdh_data(schema::PDPData* pdp_buf);
  void update_sim_data(schema::PDPData* pdp_buf);

  HAL_PowerDistributionHandle pd_handle;
  HAL_CANHandle pd_can_handle;
  HAL_PowerDistributionType pd_type;

  HAL_RuntimeType runtime;
};