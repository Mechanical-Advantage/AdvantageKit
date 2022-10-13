#pragma once

#include "conduit/conduit_schema_generated.h"
using namespace org::littletonrobotics::conduit;

#include <hal/PowerDistribution.h>

#include <atomic>
#include <mutex>
#include <thread>

// Reads system stats data from the RIO. The data is read synchronously as
// there are no periodic updates (unlike DS and PD data).
class SystemReader {
 public:
  void read(schema::SystemData* system_buf);
};