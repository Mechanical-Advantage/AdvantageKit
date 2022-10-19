#pragma once

#include "conduit/conduit_schema_generated.h"
using namespace org::littletonrobotics::conduit;

#include <thread>
#include <mutex>
#include <atomic>

#include <jni.h>

#include <hal/CANAPITypes.h>
#include <hal/PowerDistribution.h>
#include <hal/HALBase.h>

// Reads data from the power distribution panel.  The data is read in a thread and copied
// into a schema::PDPData internal buffer.  This copying is done under lock. When
// a read is requested, the same lock is acquired.  This minimizes contention
// between the costly PDP read functions and the reading of data into the buffer.
// The copy between the internal buffer and the external buffer is a single copy
// operation since the two buffers are the same structure
class PDPReader {
 public:
  PDPReader();
  ~PDPReader();
  void start();
  void read(schema::PDPData* pdp_buf);
  void configure(JNIEnv *env, jint module, jint type, schema::PDPData *pdp_buf);

 private:
  std::timed_mutex copy_mutex;

  void update_pd_data(schema::PDPData *pdp_buf);
  void update_ctre_pdp_data(schema::PDPData *pdp_buf);
  void update_rev_pdh_data(schema::PDPData *pdp_buf);
  void update_sim_data(schema::PDPData *pdp_buf);

  HAL_PowerDistributionHandle pd_handle;
  HAL_CANHandle pd_can_handle;
  HAL_PowerDistributionType pd_type;

  HAL_RuntimeType runtime;
};