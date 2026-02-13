// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include <networktables/GenericEntry.h>
#include <wpi/json.h>
#include "akit/networktables/NT4Publisher.h"

using namespace akit::nt;

NT4Publisher::NT4Publisher() : akitTable {
		::nt::NetworkTableInstance::GetDefault().GetTable("/AdvantageKit") }, timestampPublisher {
		akitTable->GetIntegerTopic(TIMESTAMP_KEY.substr(1)).Publish( {
				.sendAll = true }) } {
}

void NT4Publisher::putTable(LogTable &table) {
	timestampPublisher.Set(
			units::microsecond_t { table.getTimestamp() }.value(),
			units::microsecond_t { table.getTimestamp() }.value());

	std::unordered_map < std::string, LogTable::LogValue > newMap =
			table.getAll(false);
	std::unordered_map < std::string, LogTable::LogValue > oldMap =
			lastTable.getAll(false);

	for (const auto &field : newMap) {
		if (field.second == oldMap.at(field.first))
			continue;

		std::string key = field.first.substr(1);
		std::string unit = field.second.unitStr;
		auto publisher = publishers.find(key);
		if (publisher == publishers.end()) {
			publisher =
					publishers.emplace(key,
							akitTable->GetTopic(key).GenericPublish(
									field.second.getNT4Type(),
									{ .sendAll = true })).first;
			if (!unit.empty()) {
				akitTable->GetTopic(key).SetProperty("unit",
						"\"" + unit + "\"");
				units[key] = unit;
			}
		}

		if (!unit.empty() && unit != units.at(key)) {
			akitTable->GetTopic(key).SetProperty("unit", "\"" + unit + "\"");
			units[key] = unit;
		}

		switch (field.second.type) {
		case LogTable::LoggableType::Raw: {
			auto value = field.second.getRaw();
			publisher->second.SetRaw(std::span {
					reinterpret_cast<uint8_t*>(value.data()), value.size() },
					units::microsecond_t { table.getTimestamp() }.value());
			break;
		}
		case LogTable::LoggableType::Boolean:
			publisher->second.SetBoolean(field.second.getBoolean(),
					units::microsecond_t { table.getTimestamp() }.value());
			break;
		case LogTable::LoggableType::BooleanArray: {
			auto value = field.second.getBooleanArray();
			publisher->second.SetBooleanArray(std::vector<int> { value.begin(),
					value.end() },
					units::microsecond_t { table.getTimestamp() }.value());
			break;
		}
		case LogTable::LoggableType::Integer:
			publisher->second.SetInteger(field.second.getInteger(),
					units::microsecond_t { table.getTimestamp() }.value());
			break;
		case LogTable::LoggableType::IntegerArray: {
			auto value = field.second.getIntegerArray();
			publisher->second.SetIntegerArray(std::vector<int64_t> {
					value.begin(), value.end() },
					units::microsecond_t { table.getTimestamp() }.value());
			break;
		}
		case LogTable::LoggableType::Float:
			publisher->second.SetFloat(field.second.getFloat(),
					units::microsecond_t { table.getTimestamp() }.value());
			break;
		case LogTable::LoggableType::FloatArray:
			publisher->second.SetFloatArray(field.second.getFloatArray(),
					units::microsecond_t { table.getTimestamp() }.value());
			break;
		case LogTable::LoggableType::Double:
			publisher->second.SetDouble(field.second.getDouble(),
					units::microsecond_t { table.getTimestamp() }.value());
			break;
		case LogTable::LoggableType::DoubleArray:
			publisher->second.SetDoubleArray(field.second.getDoubleArray(),
					units::microsecond_t { table.getTimestamp() }.value());
			break;
		case LogTable::LoggableType::String:
			publisher->second.SetString(field.second.getString(),
					units::microsecond_t { table.getTimestamp() }.value());
			break;
		case LogTable::LoggableType::StringArray:
			publisher->second.SetStringArray(field.second.getStringArray(),
					units::microsecond_t { table.getTimestamp() }.value());
			break;
		}
	}

	lastTable = table;
}
