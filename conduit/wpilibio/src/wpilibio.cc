#include "conduit/wpilibio/include/wpilibio.h"

#include <hal/DriverStation.h>
#include <hal/DriverStationTypes.h>
#include <hal/HALBase.h>
#include <jni.h>
#include <stdlib.h>

#include <cstdint>
#include <iostream>

#include "conduit/conduit_schema_generated.h"
#include "conduit/wpilibio/include/ds_reader.h"
#include "conduit/wpilibio/include/pdp_reader.h"
#include "conduit/wpilibio/include/system_reader.h"

namespace akit {
namespace conduit {
namespace wpilibio {

using namespace org::littletonrobotics::conduit;

void* shared_buf = 0;
schema::CoreInputs* corein_view;
schema::DSData* ds_view;
schema::PDPData* pdp_view;
schema::SystemData* sys_view;

DsReader ds_reader;
PDPReader pdp_reader;
SystemReader sys_reader;

void start() { ds_reader.start(); }

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

void configurePDP(JNIEnv* env, jint moduleNumber, jint type) {
  pdp_reader.configure(env, moduleNumber, type, pdp_view);
}

}  // namespace wpilibio
}  // namespace conduit
}  // namespace akit