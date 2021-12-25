#include "conduit/wpilibio/include/wpilibio.h"

#include <hal/DriverStation.h>
#include <hal/DriverStationTypes.h>
#include <hal/HALBase.h>
#include <stdlib.h>

#include <cstdint>

#include "conduit/conduit_schema_generated.h"
#include "conduit/wpilibio/include/ds_reader.h"

namespace akit {
namespace conduit {
namespace wpilibio {

using namespace org::littletonrobotics::conduit;

void* shared_buf = 0;
schema::CoreInputs* corein_view;
schema::DSData* ds_view;

DsReader ds_reader;

void start() {
  ds_reader.start();
}

void make_buffer() {
  // Allocate shared buffer
  shared_buf = malloc(BUF_SIZE);

  // Point view pointers at the buffer at the right offset
  corein_view = reinterpret_cast<schema::CoreInputs*>(shared_buf);
  ds_view = &corein_view->mutable_ds();
}

void capture_data(void) {
  std::int32_t status;

  corein_view->mutate_timestamp(HAL_GetFPGATime(&status));
  ds_reader.read(ds_view);
}

}  // namespace wpilibio
}  // namespace conduit
}  // namespace akit