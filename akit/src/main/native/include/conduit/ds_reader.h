// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once

#include "conduit_schema_generated.h"
using namespace org::littletonrobotics::conduit;

#include <atomic>
#include <mutex>
#include <thread>

// Reads data from the driver station. The data is synchronously because it must
// be aligned with the main loop cycle.
class DsReader {
public:
	void read(schema::DSData *ds_buf);
};
