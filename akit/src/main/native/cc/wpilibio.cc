// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include "conduit/wpilibio.h"

#include <hal/DriverStation.h>
#include <hal/DriverStationTypes.h>
#include <hal/HALBase.h>
#include <jni.h>
#include <stdlib.h>

#include <cstdint>
#include <iostream>

#include "conduit/ds_reader.h"
#include "conduit/pdp_reader.h"
#include "conduit/system_reader.h"
#include "conduit_schema_generated.h"

namespace akit {
namespace conduit {
namespace wpilibio {

using namespace org::littletonrobotics::conduit;

void *shared_buf = 0;
schema::CoreInputs *corein_view;
schema::DSData *ds_view;
schema::PDPData *pdp_view;
schema::SystemData *sys_view;

DsReader ds_reader;
PDPReader pdp_reader;
SystemReader sys_reader;

void start() {
}

void make_buffer() {
	// Allocate shared buffer
	shared_buf = malloc(BUF_SIZE);

	// Point view pointers at the buffer at the right offset
	corein_view = reinterpret_cast<schema::CoreInputs*>(shared_buf);
	ds_view = &corein_view->mutable_ds();
	pdp_view = &corein_view->mutable_pdp();
	sys_view = &corein_view->mutable_sys();
}

void capture_data(void) {
	std::int32_t status;

	corein_view->mutate_timestamp(HAL_GetFPGATime(&status));
	ds_reader.read(ds_view);
	pdp_reader.read(pdp_view);
	sys_reader.read(sys_view);
}

void configurePDP(JNIEnv *env, jint moduleNumber, jint type) {
	pdp_reader.configure(env, moduleNumber, type, pdp_view);
}

}  // namespace wpilibio
}  // namespace conduit
}  // namespace akit
