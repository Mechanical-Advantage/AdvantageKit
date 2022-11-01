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
  void read(schema::DSData* ds_buf);
};