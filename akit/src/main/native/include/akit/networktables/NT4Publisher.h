// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once
#include <networktables/NetworkTable.h>
#include <networktables/NetworkTableInstance.h>
#include <networktables/IntegerTopic.h>
#include "akit/LogDataReceiver.h"

namespace akit {

namespace nt {

class NT4Publisher: LogDataReceiver {
public:
	NT4Publisher();

	void putTable(LogTable &table) override;

private:
	std::shared_ptr<::nt::NetworkTable> akitTable;
	LogTable lastTable { 0 };
	::nt::IntegerPublisher timestampPublisher;
	std::unordered_map<std::string, ::nt::GenericPublisher> publishers;
	std::unordered_map<std::string, std::string> units;
};

}

}
