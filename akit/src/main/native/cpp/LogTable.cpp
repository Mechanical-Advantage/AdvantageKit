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
LogTable::LogValue::LogValue(float value, std::string typeStr) : type {
		LoggableType::Float }, customTypeStr { typeStr }, value { value } {
}
LogTable::LogValue::LogValue(float value, std::string typeStr,
		std::string unitStr) : type { LoggableType::Float }, customTypeStr {
		customTypeStr }, unitStr { unitStr }, value { value } {
}
LogTable::LogValue::LogValue(double value, std::string typeStr) : type {
		LoggableType::Double }, customTypeStr { typeStr }, value { value } {
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
