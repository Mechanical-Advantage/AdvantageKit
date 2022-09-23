#include "conduit/wpilibio/include/pdp_reader.h"

#include <hal/PowerDistribution.h>
#include <hal/HALBase.h>

#include <chrono>
#include <cstdint>
#include <cstring>
#include <iostream>
#include <mutex>

using namespace std::chrono_literals;

PDPReader::PDPReader() : is_running(false) {}

PDPReader::~PDPReader() {
  if (is_running) {
    is_running = false;    // Stop the thread when we destruct this object
    pdp_thread.join();
  }
}

void PDPReader::start() {
  pdp_thread = std::thread(&PDPReader::update_pdp_data, this);
}

void PDPReader::update_pdp_data() {
  is_running = true;
  while (is_running) {
    std::int32_t status;

    // Copy all data into the internal buffer
    copy_mutex.lock();
    
    copy_mutex.unlock();
  }
}

void PDPReader::read(schema::PDPData* pdp_buf) {
  if (copy_mutex.try_lock_for(5s)) {
    std::memcpy(pdp_buf, &internal_buf, sizeof(schema::PDPData));
    copy_mutex.unlock();
  } else {
    std::cout
        << "[conduit] Could not acquire PDP read lock after 5 seconds!  Exiting!"
        << std::endl;
    exit(1);
  }
}