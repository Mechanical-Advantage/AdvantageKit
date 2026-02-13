// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include "akit/LogTable.h"

using namespace akit;

LogTable::LogValue::LogValue(std::vector<std::byte> value, std::string typeStr) : type {
		LoggableType::String }, customTypeStr { typeStr }, value { value } {
}
LogTable::LogValue::LogValue(bool value, std::string typeStr) : type {
		LoggableType::Boolean }, customTypeStr { typeStr }, value { value } {
}
LogTable::LogValue::LogValue(long value, std::string typeStr) : type {
		LoggableType::Integer }, customTypeStr { typeStr }, value { value } {
}
LogTable::LogValue::LogValue(float value, std::string typeStr,
		std::string unitStr) : type { LoggableType::Float }, customTypeStr {
		typeStr }, unitStr { unitStr }, value { value } {
}
LogTable::LogValue::LogValue(double value, std::string typeStr,
		std::string unitStr) : type { LoggableType::Double }, customTypeStr {
		typeStr }, unitStr { unitStr }, value { value } {
}
LogTable::LogValue::LogValue(std::string value, std::string typeStr) : type {
		LoggableType::String }, customTypeStr { typeStr }, value { value } {
}
LogTable::LogValue::LogValue(std::vector<bool> value, std::string typeStr) : type {
		LoggableType::BooleanArray }, customTypeStr { typeStr }, value { value } {
}
LogTable::LogValue::LogValue(std::vector<long> value, std::string typeStr) : type {
		LoggableType::IntegerArray }, customTypeStr { typeStr }, value { value } {
}
LogTable::LogValue::LogValue(std::vector<float> value, std::string typeStr) : type {
		LoggableType::FloatArray }, customTypeStr { typeStr }, value { value } {
}
LogTable::LogValue::LogValue(std::vector<double> value, std::string typeStr) : type {
		LoggableType::DoubleArray }, customTypeStr { typeStr }, value { value } {
}
LogTable::LogValue::LogValue(std::vector<std::string> value,
		std::string typeStr) : type { LoggableType::StringArray }, customTypeStr {
		typeStr }, value { value } {
}

std::vector<std::byte> LogTable::LogValue::getRaw(
		std::vector<std::byte> defaultValue) const {
	return type == LoggableType::Raw ?
			std::any_cast < std::vector < std::byte >> (value) : defaultValue;
}

bool LogTable::LogValue::getBoolean(bool defaultValue) const {
	return type == LoggableType::Boolean ?
			std::any_cast<bool>(value) : defaultValue;
}

long LogTable::LogValue::getInteger(long defaultValue) const {
	return type == LoggableType::Integer ?
			std::any_cast<long>(value) : defaultValue;
}

float LogTable::LogValue::getFloat(float defaultValue) const {
	return type == LoggableType::Float ?
			std::any_cast<float>(value) : defaultValue;
}

double LogTable::LogValue::getDouble(double defaultValue) const {
	return type == LoggableType::Double ?
			std::any_cast<double>(value) : defaultValue;
}

std::string LogTable::LogValue::getString(std::string defaultValue) const {
	return type == LoggableType::String ?
			std::any_cast < std::string > (value) : defaultValue;
}

std::vector<bool> LogTable::LogValue::getBooleanArray(
		std::vector<bool> defaultValue) const {
	return type == LoggableType::BooleanArray ?
			std::any_cast<std::vector<bool>>(value) : defaultValue;
}

std::vector<long> LogTable::LogValue::getIntegerArray(
		std::vector<long> defaultValue) const {
	return type == LoggableType::IntegerArray ?
			std::any_cast<std::vector<long>>(value) : defaultValue;
}

std::vector<float> LogTable::LogValue::getFloatArray(
		std::vector<float> defaultValue) const {
	return type == LoggableType::FloatArray ?
			std::any_cast<std::vector<float>>(value) : defaultValue;
}

std::vector<double> LogTable::LogValue::getDoubleArray(
		std::vector<double> defaultValue) const {
	return type == LoggableType::DoubleArray ?
			std::any_cast<std::vector<double>>(value) : defaultValue;
}

std::vector<std::string> LogTable::LogValue::getStringArray(
		std::vector<std::string> defaultValue) const {
	return type == LoggableType::StringArray ?
			std::any_cast < std::vector < std::string >> (value) : defaultValue;
}

std::string LogTable::LogValue::getWPILOGType() const {
	if (customTypeStr.empty())
		return std::string { WPILOG_TYPES[static_cast<int>(type)] };
	return customTypeStr;
}

std::string LogTable::LogValue::getNT4Type() const {
	if (customTypeStr.empty())
		return std::string { NT4_TYPES[static_cast<int>(type)] };
	return customTypeStr;
}

bool LogTable::LogValue::operator==(const LogValue &other) const {
	if (other.type == type && customTypeStr == other.customTypeStr
			&& unitStr == other.unitStr
			&& (customTypeStr.empty() || other.customTypeStr == customTypeStr)
			&& (unitStr.empty() || other.unitStr == unitStr)) {
		switch (type) {
		case LoggableType::Raw:
			return getRaw() == other.getRaw();
		case LoggableType::Boolean:
			return getBoolean() == other.getBoolean();
		case LoggableType::Integer:
			return getInteger() == other.getInteger();
		case LoggableType::Float:
			return getFloat() == other.getFloat();
		case LoggableType::Double:
			return getDouble() == other.getDouble();
		case LoggableType::String:
			return getString() == other.getString();
		case LoggableType::BooleanArray:
			return getBooleanArray() == other.getBooleanArray();
		case LoggableType::IntegerArray:
			return getIntegerArray() == other.getIntegerArray();
		case LoggableType::FloatArray:
			return getFloatArray() == other.getFloatArray();
		case LoggableType::DoubleArray:
			return getDoubleArray() == other.getDoubleArray();
		case LoggableType::StringArray:
			return getStringArray() == other.getStringArray();
		}
	}
	return false;
}

std::unordered_map<std::string, LogTable::LogValue> LogTable::getAll(
		bool subtableOnly) {
	if (subtableOnly) {
		std::unordered_map < std::string, LogValue > result;
		for (const auto &field : data) {
			if (field.first.starts_with(prefix))
				result.emplace(field.first.substr(prefix.size()), field.second);
		}
		return result;
	} else
		return data;
}

bool LogTable::writeAllowed(std::string key, LoggableType type,
		std::string customTypeStr) {
	auto currentValue = data.find(prefix + key);
	if (currentValue == data.end())
		return true;
	if (currentValue->second.type != type) {
		FRC_ReportWarning(
				"[AdvantageKit] Failed to write to field \"{}{}\" - attempted to write {} value but expected {}",
				prefix, key, magic_enum::enum_name(type),
				magic_enum::enum_name(currentValue->second.type));
		return false;
	}
	if (currentValue->second.customTypeStr != customTypeStr) {
		FRC_ReportWarning(
				"[AdvantageKit] Failed to write to field \"{}{}\" - attempted to write {} value but expected {}",
				prefix, key, customTypeStr, currentValue->second.customTypeStr);
		return false;
	}
	return true;
}

void LogTable::put(std::string key, LogTable::LogValue value) {
	if (writeAllowed(key, value.type, value.customTypeStr))
		data.emplace(prefix + key, value);
}

void LogTable::put(std::string key, std::vector<std::vector<std::byte>> value) {
	put(key + "/length", static_cast<long>(value.size()));
	for (size_t i = 0; i < value.size(); i++)
		put(key + "/" + std::to_string(i), value[i]);
}

void LogTable::put(std::string key, std::vector<std::vector<bool>> value) {
	put(key + "/length", static_cast<long>(value.size()));
	for (size_t i = 0; i < value.size(); i++)
		put(key + "/" + std::to_string(i), value[i]);
}

void LogTable::put(std::string key, std::vector<std::vector<long>> value) {
	put(key + "/length", static_cast<long>(value.size()));
	for (size_t i = 0; i < value.size(); i++)
		put(key + "/" + std::to_string(i), value[i]);
}

void LogTable::put(std::string key, std::vector<std::vector<float>> value) {
	put(key + "/length", static_cast<long>(value.size()));
	for (size_t i = 0; i < value.size(); i++)
		put(key + "/" + std::to_string(i), value[i]);
}

void LogTable::put(std::string key, std::vector<std::vector<double>> value) {
	put(key + "/length", static_cast<long>(value.size()));
	for (size_t i = 0; i < value.size(); i++)
		put(key + "/" + std::to_string(i), value[i]);
}

void LogTable::put(std::string key,
		std::vector<std::vector<std::string>> value) {
	put(key + "/length", static_cast<long>(value.size()));
	for (size_t i = 0; i < value.size(); i++)
		put(key + "/" + std::to_string(i), value[i]);
}

void LogTable::addStructSchema(std::string typeString, std::string schema,
		std::unordered_set<std::string> &seen) {
	std::string key = "/.schema/" + typeString;

	if (data.contains(key))
		return;
	seen.insert(typeString);

	data.emplace(key,
			LogValue {
					std::vector<std::byte> {
							reinterpret_cast<std::byte*>(schema.data()),
							reinterpret_cast<std::byte*>(schema.data())
									+ schema.size() }, "" });
}

std::vector<std::byte> LogTable::get(std::string key,
		std::vector<std::byte> defaultValue) {
	if (data.contains(prefix + key))
		return get(key).getRaw(defaultValue);
	else
		return defaultValue;
}

std::vector<std::vector<std::byte>> LogTable::get(std::string key,
		std::vector<std::vector<std::byte>> defaultValue) {
	if (data.contains(prefix + key + "/length")) {
		std::vector < std::vector < std::byte >> value {
				static_cast<size_t>(get(key + "/length", 0L)) };
		for (size_t i = 0; i < value.size(); i++)
			value[i] = get(key + "/" + std::to_string(i),
					std::vector<std::byte> { });
		return value;
	} else
		return defaultValue;
}

bool LogTable::get(std::string key, bool defaultValue) {
	if (data.contains(prefix + key))
		return get(key).getBoolean(defaultValue);
	else
		return defaultValue;
}

std::vector<bool> LogTable::get(std::string key,
		std::vector<bool> defaultValue) {
	if (data.contains(prefix + key))
		return get(key).getBooleanArray(defaultValue);
	else
		return defaultValue;
}

std::vector<std::vector<bool>> LogTable::get(std::string key,
		std::vector<std::vector<bool>> defaultValue) {
	if (data.contains(prefix + key + "/length")) {
		std::vector<std::vector<bool>> value { static_cast<size_t>(get(
				key + "/length", 0L)) };
		for (size_t i = 0; i < value.size(); i++)
			value[i] = get(key + "/" + std::to_string(i),
					std::vector<bool> { });
		return value;
	} else
		return defaultValue;
}

long LogTable::get(std::string key, long defaultValue) {
	if (data.contains(prefix + key))
		return get(key).getInteger(defaultValue);
	else
		return defaultValue;
}

std::vector<long> LogTable::get(std::string key,
		std::vector<long> defaultValue) {
	if (data.contains(prefix + key))
		return get(key).getIntegerArray(defaultValue);
	else
		return defaultValue;
}

std::vector<std::vector<long>> LogTable::get(std::string key,
		std::vector<std::vector<long>> defaultValue) {
	if (data.contains(prefix + key + "/length")) {
		std::vector<std::vector<long>> value { static_cast<size_t>(get(
				key + "/length", 0L)) };
		for (size_t i = 0; i < value.size(); i++)
			value[i] = get(key + "/" + std::to_string(i),
					std::vector<long> { });
		return value;
	} else
		return defaultValue;
}

float LogTable::get(std::string key, float defaultValue) {
	if (data.contains(prefix + key))
		return get(key).getFloat(defaultValue);
	else
		return defaultValue;
}

std::vector<float> LogTable::get(std::string key,
		std::vector<float> defaultValue) {
	if (data.contains(prefix + key))
		return get(key).getFloatArray(defaultValue);
	else
		return defaultValue;
}

std::vector<std::vector<float>> LogTable::get(std::string key,
		std::vector<std::vector<float>> defaultValue) {
	if (data.contains(prefix + key + "/length")) {
		std::vector<std::vector<float>> value { static_cast<size_t>(get(
				key + "/length", 0L)) };
		for (size_t i = 0; i < value.size(); i++)
			value[i] = get(key + "/" + std::to_string(i),
					std::vector<float> { });
		return value;
	} else
		return defaultValue;
}

double LogTable::get(std::string key, double defaultValue) {
	if (data.contains(prefix + key))
		return get(key).getInteger(defaultValue);
	else
		return defaultValue;
}

std::vector<double> LogTable::get(std::string key,
		std::vector<double> defaultValue) {
	if (data.contains(prefix + key))
		return get(key).getDoubleArray(defaultValue);
	else
		return defaultValue;
}

std::vector<std::vector<double>> LogTable::get(std::string key,
		std::vector<std::vector<double>> defaultValue) {
	if (data.contains(prefix + key + "/length")) {
		std::vector<std::vector<double>> value { static_cast<size_t>(get(
				key + "/length", 0L)) };
		for (size_t i = 0; i < value.size(); i++)
			value[i] = get(key + "/" + std::to_string(i),
					std::vector<double> { });
		return value;
	} else
		return defaultValue;
}

std::string LogTable::get(std::string key, std::string defaultValue) {
	if (data.contains(prefix + key))
		return get(key).getString(defaultValue);
	else
		return defaultValue;
}

std::vector<std::string> LogTable::get(std::string key,
		std::vector<std::string> defaultValue) {
	if (data.contains(prefix + key))
		return get(key).getStringArray(defaultValue);
	else
		return defaultValue;
}

std::vector<std::vector<std::string>> LogTable::get(std::string key,
		std::vector<std::vector<std::string>> defaultValue) {
	if (data.contains(prefix + key + "/length")) {
		std::vector < std::vector < std::string >> value {
				static_cast<size_t>(get(key + "/length", 0L)) };
		for (size_t i = 0; i < value.size(); i++)
			value[i] = get(key + "/" + std::to_string(i),
					std::vector<std::string> { });
		return value;
	} else
		return defaultValue;
}

frc::Color LogTable::get(std::string key, frc::Color defaultValue) {
	if (data.contains(prefix + key))
		return frc::Color { get(key).getString(defaultValue.HexString()) };
	else
		return defaultValue;
}
