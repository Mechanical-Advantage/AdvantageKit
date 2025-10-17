// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once

#include <conduit_schema.pb.h>
#include <wpi/DataLog.h>

namespace akit {
namespace conduit {
namespace wpilibio {

class ProtoLogger {
public:
  ProtoLogger() = delete;

  static void start(wpi::log::DataLog &log);

  static void log(const int seqnum, const std::string &inputs);

private:
  static bool running;
  static wpi::log::RawLogEntry schemaEntry;
  static wpi::log::IntegerLogEntry seqnumEntry;
  static wpi::log::IntegerLogEntry sizeEntry;
  static wpi::log::RawLogEntry protoEntry;
};

} // namespace wpilibio
} // namespace conduit
} // namespace akit
