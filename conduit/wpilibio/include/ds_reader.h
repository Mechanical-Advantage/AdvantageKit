#pragma once

#include "conduit/conduit_schema_generated.h"
using namespace org::littletonrobotics::conduit;

#include <thread>
#include <mutex>
#include <atomic>

// Reads data from the driver station.  The data is read in a thread and copied
// into a schema::DSData internal buffer.  This copying is done under lock. When
// a read is requested, the same lock is acquired.  This minimizes contention
// between the costly DS read functions and the reading of data into the buffer.
// The copy between the internal buffer and the external buffer is a single copy
// operation since the two buffers are the same structure
class DsReader {
 public:
  DsReader();
  ~DsReader();
  void start();
  void read(schema::DSData* ds_buf);

 private:
  // Thread to read data from the driver station
  std::thread ds_thread;
  std::atomic<bool> is_running;

  std::mutex copy_mutex;

  // Function called by the thread
  void update_ds_data();

  // Internal buffer used to store the read data
  schema::DSData internal_buf;
};