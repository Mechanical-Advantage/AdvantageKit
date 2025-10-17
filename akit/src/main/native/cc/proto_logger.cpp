// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include <ProtoLogger.h>
#include <google/protobuf/descriptor.h>
#include <google/protobuf/descriptor.pb.h>

namespace akit {
namespace conduit {
namespace wpilibio {

bool ProtoLogger::running = false;
wpi::log::RawLogEntry ProtoLogger::schemaEntry;
wpi::log::IntegerLogEntry ProtoLogger::seqnumEntry;
wpi::log::IntegerLogEntry ProtoLogger::sizeEntry;
wpi::log::RawLogEntry ProtoLogger::protoEntry;

void ProtoLogger::start(wpi::log::DataLog& log) {
  running = true;

  // Create entries
  schemaEntry =
      wpi::log::RawLogEntry(log, "/.schema/proto:conduit_schema.proto", "",
                            "proto:FileDescriptorProto");
  seqnumEntry = wpi::log::IntegerLogEntry(log, "ProtoSeqnum");
  sizeEntry = wpi::log::IntegerLogEntry(log, "ProtoSize");
  protoEntry = wpi::log::RawLogEntry(
      log, "ProtoTest", "",
      "proto:org.littletonrobotics.conduit.schema.CoreInputs");

  // Add schema value
  const auto fileDescriptor =
      google::protobuf::DescriptorPool::generated_pool()->FindFileByName(
          "conduit_schema.proto");
  if (fileDescriptor) {
    google::protobuf::FileDescriptorProto fileDescriptorProto;
    fileDescriptor->CopyTo(&fileDescriptorProto);
    std::string serializedDescriptor;
    if (fileDescriptorProto.SerializeToString(&serializedDescriptor)) {
      schemaEntry.Append(
          {reinterpret_cast<const uint8_t*>(serializedDescriptor.data()),
           serializedDescriptor.size()});
    }
  }
};

void ProtoLogger::log(const int seqnum, const std::string& inputs) {
  if (running) {
    const auto now = wpi::Now();
    seqnumEntry.Update(seqnum, now);
    sizeEntry.Update(inputs.size(), now);
    protoEntry.Append(
        {reinterpret_cast<const uint8_t*>(inputs.data()), inputs.size()}, now);
  }
};

}  // namespace wpilibio
}  // namespace conduit
}  // namespace akit
