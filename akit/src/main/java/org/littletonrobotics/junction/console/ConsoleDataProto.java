// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction.console;

import org.littletonrobotics.junction.schema.ProtobufConsoleData;
import org.wpilib.util.protobuf.Protobuf;
import us.hebi.quickbuf.Descriptors.Descriptor;

public class ConsoleDataProto implements Protobuf<ConsoleData, ProtobufConsoleData> {
  @Override
  public Class<ConsoleData> getTypeClass() {
    return ConsoleData.class;
  }

  @Override
  public Descriptor getDescriptor() {
    return ProtobufConsoleData.getDescriptor();
  }

  @Override
  public ProtobufConsoleData createMessage() {
    return ProtobufConsoleData.newInstance();
  }

  @Override
  public ConsoleData unpack(ProtobufConsoleData msg) {
    return new ConsoleData(msg.getData(), msg.getIndex());
  }

  @Override
  public void pack(ProtobufConsoleData msg, ConsoleData value) {
    msg.setData(value.data);
    msg.setIndex(value.index);
  }
}
