// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction.console;

import org.wpilib.util.protobuf.ProtobufSerializable;

/** Serializable data structure representing a console update to be published. */
public class ConsoleData implements ProtobufSerializable {
  /** The Proto schema for serializing and logging ConsoleData */
  public static final ConsoleDataProto proto = new ConsoleDataProto();

  /** The text that the console has outputted since the last cycle. */
  public String data;

  /** An index of console data updates to distinguish between identical lines. */
  public int index;

  /**
   * Creates a new ConsoleData object with the specified starting data.
   *
   * @param data The initial console text
   * @param index The initial index
   */
  public ConsoleData(String data, int index) {
    this.data = data;
    this.index = index;
  }
}
