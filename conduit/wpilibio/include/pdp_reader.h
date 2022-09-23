#pragma once

#include "conduit/conduit_schema_generated.h"
using namespace org::littletonrobotics::conduit;

#include <thread>
#include <mutex>
#include <atomic>

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

 private:
  // Thread to read data from the power distribution panel
  std::thread pdp_thread;
  std::atomic<bool> is_running;

  std::timed_mutex copy_mutex;

  // Function called by the thread
  void update_pdp_data();

  // Internal buffer used to store the read data
  schema::PDPData internal_buf;
};