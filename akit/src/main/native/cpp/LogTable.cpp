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

std::vector<std::byte> LogTable::LogValue::GetRaw(
		std::vector<std::byte> defaultValue) const {
	return type == LoggableType::Raw ?
			std::any_cast < std::vector < std::byte >> (value) : defaultValue;
}

bool LogTable::LogValue::GetBoolean(bool defaultValue) const {
	return type == LoggableType::Boolean ?
			std::any_cast<bool>(value) : defaultValue;
}

long LogTable::LogValue::GetInteger(long defaultValue) const {
	return type == LoggableType::Integer ?
			std::any_cast<long>(value) : defaultValue;
}

float LogTable::LogValue::GetFloat(float defaultValue) const {
	return type == LoggableType::Float ?
			std::any_cast<float>(value) : defaultValue;
}

double LogTable::LogValue::GetDouble(double defaultValue) const {
	return type == LoggableType::Double ?
			std::any_cast<double>(value) : defaultValue;
}

std::string LogTable::LogValue::GetString(std::string defaultValue) const {
	return type == LoggableType::String ?
			std::any_cast < std::string > (value) : defaultValue;
}

std::vector<bool> LogTable::LogValue::GetBooleanArray(
		std::vector<bool> defaultValue) const {
	return type == LoggableType::BooleanArray ?
			std::any_cast<std::vector<bool>>(value) : defaultValue;
}

std::vector<long> LogTable::LogValue::GetIntegerArray(
		std::vector<long> defaultValue) const {
	return type == LoggableType::IntegerArray ?
			std::any_cast<std::vector<long>>(value) : defaultValue;
}

std::vector<float> LogTable::LogValue::GetFloatArray(
		std::vector<float> defaultValue) const {
	return type == LoggableType::FloatArray ?
			std::any_cast<std::vector<float>>(value) : defaultValue;
}

std::vector<double> LogTable::LogValue::GetDoubleArray(
		std::vector<double> defaultValue) const {
	return type == LoggableType::DoubleArray ?
			std::any_cast<std::vector<double>>(value) : defaultValue;
}

std::vector<std::string> LogTable::LogValue::GetStringArray(
		std::vector<std::string> defaultValue) const {
	return type == LoggableType::StringArray ?
			std::any_cast < std::vector < std::string >> (value) : defaultValue;
}

std::string LogTable::LogValue::GetWPILOGType() const {
	if (customTypeStr.empty())
		return std::string { WPILOG_TYPES[static_cast<int>(type)] };
	return customTypeStr;
}

std::string LogTable::LogValue::GetNT4Type() const {
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
			return GetRaw() == other.GetRaw();
		case LoggableType::Boolean:
			return GetBoolean() == other.GetBoolean();
		case LoggableType::Integer:
			return GetInteger() == other.GetInteger();
		case LoggableType::Float:
			return GetFloat() == other.GetFloat();
		case LoggableType::Double:
			return GetDouble() == other.GetDouble();
		case LoggableType::String:
			return GetString() == other.GetString();
		case LoggableType::BooleanArray:
			return GetBooleanArray() == other.GetBooleanArray();
		case LoggableType::IntegerArray:
			return GetIntegerArray() == other.GetIntegerArray();
		case LoggableType::FloatArray:
			return GetFloatArray() == other.GetFloatArray();
		case LoggableType::DoubleArray:
			return GetDoubleArray() == other.GetDoubleArray();
		case LoggableType::StringArray:
			return GetStringArray() == other.GetStringArray();
		}
	}
	return false;
}

std::unordered_map<std::string, LogTable::LogValue> LogTable::GetAll(
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

bool LogTable::WriteAllowed(std::string key, LoggableType type,
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

void LogTable::Put(std::string key, LogTable::LogValue value) {
	if (WriteAllowed(key, value.type, value.customTypeStr))
		data.emplace(prefix + key, value);
}

void LogTable::AddStructSchema(std::string typeString, std::string schema,
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

std::vector<std::byte> LogTable::Get(std::string key,
		std::vector<std::byte> defaultValue) {
	if (data.contains(prefix + key))
		return Get(key).GetRaw(defaultValue);
	else
		return defaultValue;
}

bool LogTable::Get(std::string key, bool defaultValue) {
	if (data.contains(prefix + key))
		return Get(key).GetBoolean(defaultValue);
	else
		return defaultValue;
}

std::vector<bool> LogTable::Get(std::string key,
		std::vector<bool> defaultValue) {
	if (data.contains(prefix + key))
		return Get(key).GetBooleanArray(defaultValue);
	else
		return defaultValue;
}

float LogTable::Get(std::string key, float defaultValue) {
	if (data.contains(prefix + key))
		return Get(key).GetFloat(defaultValue);
	else
		return defaultValue;
}

std::vector<float> LogTable::Get(std::string key,
		std::vector<float> defaultValue) {
	if (data.contains(prefix + key))
		return Get(key).GetFloatArray(defaultValue);
	else
		return defaultValue;
}

double LogTable::Get(std::string key, double defaultValue) {
	if (data.contains(prefix + key))
		return Get(key).GetInteger(defaultValue);
	else
		return defaultValue;
}

std::vector<double> LogTable::Get(std::string key,
		std::vector<double> defaultValue) {
	if (data.contains(prefix + key))
		return Get(key).GetDoubleArray(defaultValue);
	else
		return defaultValue;
}

std::string LogTable::Get(std::string key, std::string defaultValue) {
	if (data.contains(prefix + key))
		return Get(key).GetString(defaultValue);
	else
		return defaultValue;
}

std::vector<std::string> LogTable::Get(std::string key,
		std::vector<std::string> defaultValue) {
	if (data.contains(prefix + key))
		return Get(key).GetStringArray(defaultValue);
	else
		return defaultValue;
}

frc::Color LogTable::Get(std::string key, frc::Color defaultValue) {
	if (data.contains(prefix + key))
		return frc::Color { Get(key).GetString(defaultValue.HexString()) };
	else
		return defaultValue;
}
