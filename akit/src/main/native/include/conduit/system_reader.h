// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once

#include "conduit_schema_generated.h"
using namespace org::littletonrobotics::conduit;

#include <hal/PowerDistribution.h>

#include <atomic>
#include <mutex>
#include <thread>

// Reads system stats data from the RIO. The data is read synchronously as
// there are no periodic updates (unlike DS and PD data).
class SystemReader {
public:
	void read(schema::SystemData *system_buf);

private:
	uint64_t cycleCount = 0;
};
