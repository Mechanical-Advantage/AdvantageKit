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

#include <jni.h>

#include "conduit/wpilibio.h"
#include "org_littletonrobotics_conduit_ConduitJni.h"

JNIEXPORT jobject JNICALL
Java_org_littletonrobotics_conduit_ConduitJni_getBuffer(JNIEnv* env,
                                                        jclass clazz) {
  if (akit::conduit::wpilibio::shared_buf == 0) {
    akit::conduit::wpilibio::make_buffer();
  }
  return env->NewDirectByteBuffer(akit::conduit::wpilibio::shared_buf,
                                  akit::conduit::wpilibio::BUF_SIZE);
}

JNIEXPORT void JNICALL Java_org_littletonrobotics_conduit_ConduitJni_capture(
    JNIEnv* env, jclass clazz) {
  akit::conduit::wpilibio::capture_data();
}

JNIEXPORT void JNICALL
Java_org_littletonrobotics_conduit_ConduitJni_start(JNIEnv* env, jclass clazz) {
  akit::conduit::wpilibio::start();
}

JNIEXPORT void JNICALL
Java_org_littletonrobotics_conduit_ConduitJni_configurePowerDistribution(
    JNIEnv* env, jclass clazz, jint module, jint moduleType) {
  akit::conduit::wpilibio::configurePDP(env, module, moduleType);
}