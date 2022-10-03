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
  void configure(JNIEnv *env, jint module, jint type);

 private:
  // Thread to read data from the power distribution panel
  std::thread pdp_thread;
  std::atomic<bool> is_running;
  std::atomic<bool> should_run;

  std::timed_mutex copy_mutex;

  // Function called by the thread
  void update_pd_data();

  void update_ctre_pdp_data(uint8_t loop_counter);

  void update_rev_pdh_data(uint8_t loop_counter);
  
  void update_sim_data(uint8_t loop_counter);

  // Internal buffer used to store the read data
  schema::PDPData internal_buf;

  HAL_PowerDistributionHandle pd_handle;
  HAL_CANHandle pd_can_handle;
  HAL_PowerDistributionType pd_type;
  int32_t pd_module_id;

  HAL_RuntimeType runtime;

  std::chrono::microseconds timestamp;
  uint8_t loop_counter;
};