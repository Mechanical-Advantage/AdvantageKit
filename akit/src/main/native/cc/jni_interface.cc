// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

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
