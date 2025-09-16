// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once
#include <jni.h>

#include <cstdint>

namespace akit {
namespace conduit {
namespace wpilibio {

// Size of the data exchange buffer (CoreInputs) to use
static const int BUF_SIZE = 100000;

// Shared buffer
extern void *shared_buf;

void start();

// Allocates the shared buffer
void make_buffer();

// Captures data into the shared buffer
void capture_data();

void configurePDP(JNIEnv *env, jint moduleNumber, jint type);

}  // namespace wpilibio
}  // namespace conduit
}  // namespace akit
