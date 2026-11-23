// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction.wpilog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An OutputStream that queues written bytes in memory until a target file OutputStream is provided.
 */
class QueuedOutputStream extends OutputStream {
  private ByteArrayOutputStream queueBuffer = new ByteArrayOutputStream();
  private OutputStream fileStream = null;

  /**
   * Attaches the target file OutputStream, writing any previously queued bytes to it and forwarding
   * all future operations directly to the file stream.
   */
  public void setFileStream(OutputStream fileStream) throws IOException {
    this.fileStream = fileStream;
    if (queueBuffer != null) {
      queueBuffer.writeTo(fileStream);
      queueBuffer = null;
    }
  }

  @Override
  public void write(int b) throws IOException {
    if (fileStream != null) {
      fileStream.write(b);
    } else if (queueBuffer != null) {
      queueBuffer.write(b);
    }
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    if (fileStream != null) {
      fileStream.write(b, off, len);
    } else if (queueBuffer != null) {
      queueBuffer.write(b, off, len);
    }
  }

  @Override
  public void flush() throws IOException {
    if (fileStream != null) {
      fileStream.flush();
    }
  }

  @Override
  public void close() throws IOException {
    if (fileStream != null) {
      fileStream.close();
    }
    queueBuffer = null;
  }
}
