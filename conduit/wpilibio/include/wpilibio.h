#pragma once
#include <cstdint>

namespace akit {
namespace conduit {
namespace wpilibio {

// Size of the data exchange buffer (CoreInputs) to use
static const int BUF_SIZE = 100000;

// Shared buffer
extern void* shared_buf;

// Allocates the shared buffer
void make_buffer(void);

// Captures data into the shared buffer
void capture_data(void);

}  // namespace wpilibio
}  // namespace conduit
}  // namespace akit