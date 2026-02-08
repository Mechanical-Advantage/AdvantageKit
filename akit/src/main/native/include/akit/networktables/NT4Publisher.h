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
	NT4Publisher() : akitTable {
			::nt::NetworkTableInstance::GetDefault().GetTable("/AdvantageKit") }, timestampPublisher {
			akitTable->GetIntegerTopic(TIMESTAMP_KEY.substr(1)).Publish( {
					.sendAll = true }) } {
	}

	void putTable(LogTable table) override {
		timestampPublisher.Set(table.getTimestamp(), table.getTimestamp());

		std::unordered_map < std::string, LogTable::LogValue > newMap =
				table.getAll(false);
		std::unordered_map < std::string, LogTable::LogValue > oldMap =
				lastTable.getAll(false);

		for (const auto &field : newMap) {
			if (field.second == oldMap.at(field.first))
				continue;

			std::string key = field.first.substr(1);
			std::string unit = field.second.unitStr;
		}
	}

private:
	std::shared_ptr<::nt::NetworkTable> akitTable;
	LogTable lastTable { 0 };
	::nt::IntegerPublisher timestampPublisher;
	std::unordered_map<std::string, ::nt::GenericPublisher> publishers;
	std::unordered_map<std::string, std::string> units;
};

}

}
