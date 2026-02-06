// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once
#include <string>
#include <unordered_map>
#include <wpi/array.h>
#include <any>
#include <vector>

namespace akit {

class LogTable {
public:
	enum class LoggableType {
		Raw,
		Boolean,
		Integer,
		Float,
		Double,
		String,
		BooleanArray,
		IntegerArray,
		FloatArray,
		DoubleArray,
		StringArray
	};

	static constexpr wpi::array<std::string_view, 11> WPILOG_TYPES { "raw",
			"boolean", "int64", "float", "double", "string", "boolean[]",
			"int64[]", "float[]", "double[]", "string[]" };

	static constexpr wpi::array<std::string_view, 11> NT4_TYPES { "raw",
			"boolean", "int", "float", "double", "string", "boolean[]", "int[]",
			"float[]", "double[]", "string[]" };

	class LogValue {
	public:
		LoggableType type;
		std::string customTypeStr;
		std::string unitStr;

		LogValue(std::vector<std::byte> value, std::string typeStr);
		LogValue(bool value, std::string typeStr);
		LogValue(long value, std::string typeStr);
		LogValue(float value, std::string typeStr);
		LogValue(float value, std::string typeStr, std::string unitStr);
		LogValue(double value, std::string typeStr);
		LogValue(double value, std::string typeStr, std::string unitStr);
		LogValue(std::string value, std::string typeStr);
		LogValue(std::vector<bool> value, std::string typeStr);
		LogValue(std::vector<long> value, std::string typeStr);
		LogValue(std::vector<float> value, std::string typeStr);
		LogValue(std::vector<double> value, std::string typeStr);
		LogValue(std::vector<std::string> value, std::string typeStr);

		inline std::vector<std::byte> getRaw() const {
			return getRaw( { });
		}

		inline bool getBoolean() const {
			return getBoolean(false);
		}

		inline long getInteger() const {
			return getInteger(0);
		}

		inline float getFloat() const {
			return getFloat(0);
		}

		inline double getDouble() const {
			return getDouble(0);
		}

		inline std::string getString() const {
			return getString("");
		}

		inline std::vector<bool> getBooleanArray() const {
			return getBooleanArray( { });
		}

		inline std::vector<long> getIntegerArray() const {
			return getIntegerArray( { });
		}

		inline std::vector<float> getFloatArray() const {
			return getFloatArray( { });
		}

		inline std::vector<double> getDoubleArray() const {
			return getDoubleArray( { });
		}

		inline std::vector<std::string> getStringArray() const {
			return getStringArray( { });
		}

		inline std::vector<std::byte> getRaw(
				std::vector<std::byte> defaultValue) const {
			return type == LoggableType::Raw ?
					std::any_cast < std::vector < std::byte >> (value) :
					defaultValue;
		}

		inline bool getBoolean(bool defaultValue) const {
			return type == LoggableType::Boolean ?
					std::any_cast<bool>(value) : defaultValue;
		}

		inline long getInteger(long defaultValue) const {
			return type == LoggableType::Integer ?
					std::any_cast<long>(value) : defaultValue;
		}

		inline float getFloat(float defaultValue) const {
			return type == LoggableType::Float ?
					std::any_cast<float>(value) : defaultValue;
		}

		inline double getDouble(double defaultValue) const {
			return type == LoggableType::Double ?
					std::any_cast<double>(value) : defaultValue;
		}

		inline std::string getString(std::string defaultValue) const {
			return type == LoggableType::String ?
					std::any_cast < std::string > (value) : defaultValue;
		}

		inline std::vector<bool> getBooleanArray(
				std::vector<bool> defaultValue) const {
			return type == LoggableType::BooleanArray ?
					std::any_cast<std::vector<bool>>(value) : defaultValue;
		}

		inline std::vector<long> getIntegerArray(
				std::vector<long> defaultValue) const {
			return type == LoggableType::IntegerArray ?
					std::any_cast<std::vector<long>>(value) : defaultValue;
		}

		inline std::vector<float> getFloatArray(
				std::vector<float> defaultValue) const {
			return type == LoggableType::FloatArray ?
					std::any_cast<std::vector<float>>(value) : defaultValue;
		}

		inline std::vector<double> getDoubleArray(
				std::vector<double> defaultValue) const {
			return type == LoggableType::DoubleArray ?
					std::any_cast<std::vector<double>>(value) : defaultValue;
		}

		inline std::vector<std::string> getStringArray(
				std::vector<std::string> defaultValue) const {
			return type == LoggableType::StringArray ?
					std::any_cast < std::vector < std::string >> (value) :
					defaultValue;
		}

		inline std::string getWPILOGType() const {
			if (customTypeStr.empty())
				return std::string { WPILOG_TYPES[static_cast<int>(type)] };
			return customTypeStr;
		}

		inline std::string getNT4Type() const {
			if (customTypeStr.empty())
				return std::string { NT4_TYPES[static_cast<int>(type)] };
			return customTypeStr;
		}

		bool operator==(const LogValue &other) const {
			if (other.type == type && customTypeStr == other.customTypeStr
					&& unitStr == other.unitStr
					&& (customTypeStr.empty()
							|| other.customTypeStr == customTypeStr)
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

	private:
		std::any value;
	};

private:
	class SharedTimestamp {
	public:
		int64_t value;

		SharedTimestamp(int64_t value) : value { value } {
		}
	};

	std::string prefix;
	int depth;
	SharedTimestamp timestamp;
};

}
