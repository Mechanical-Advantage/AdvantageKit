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

#include <atomic>
#include <mutex>
#include <thread>

// Reads data from the driver station. The data is synchronously because it must
// be aligned with the main loop cycle.
class DsReader {
 public:
  void read(schema::DSData* ds_buf);
};