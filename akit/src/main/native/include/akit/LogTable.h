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
#include <memory>

#include <magic_enum/magic_enum.hpp>
#include <frc/Errors.h>
#include <frc/util/Color.h>
#include <units/base.h>

#include "akit/inputs/LoggableInputs.h"

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

		std::vector<std::byte> getRaw(
				std::vector<std::byte> defaultValue) const;

		bool getBoolean(bool defaultValue) const;

		long getInteger(long defaultValue) const;

		float getFloat(float defaultValue) const;

		double getDouble(double defaultValue) const;

		std::string getString(std::string defaultValue) const;

		std::vector<bool> getBooleanArray(std::vector<bool> defaultValue) const;

		std::vector<long> getIntegerArray(std::vector<long> defaultValue) const;

		std::vector<float> getFloatArray(std::vector<float> defaultValue) const;

		std::vector<double> getDoubleArray(
				std::vector<double> defaultValue) const;

		std::vector<std::string> getStringArray(
				std::vector<std::string> defaultValue) const;

		std::string getWPILOGType() const;

		std::string getNT4Type() const;

		bool operator==(const LogValue &other) const;

	private:
		std::any value;
	};

	LogTable(long timestamp) : LogTable { "/", 0, std::make_shared<long>(0), { } } {
	}

	inline void setTimestamp(long timestamp) {
		*this->timestamp = timestamp;
	}

	inline long getTimestamp() {
		return *this->timestamp;
	}

	inline LogTable getSubtable(std::string tableName) {
		return LogTable { prefix + tableName + "/", *this };
	}

	std::unordered_map<std::string, LogValue> getAll(bool subtableOnly);

	void put(std::string key, LogValue value);

	inline void put(std::string key, std::vector<std::byte> value) {
		put(key, LogValue { value, "" });
	}

	void put(std::string key, std::vector<std::vector<std::byte>> value);

	inline void put(std::string key, bool value) {
		put(key, LogValue { value, "" });
	}

	inline void put(std::string key, std::vector<bool> value) {
		put(key, LogValue { value, "" });
	}

	void put(std::string key, std::vector<std::vector<bool>> value);

	inline void put(std::string key, long value) {
		put(key, LogValue { value, "" });
	}

	inline void put(std::string key, std::vector<long> value) {
		put(key, LogValue(value, ""));
	}

	void put(std::string key, std::vector<std::vector<long>> value);

	inline void put(std::string key, float value) {
		put(key, LogValue { value, "" });
	}

	inline void put(std::string key, float value, std::string unitStr) {
		put(key, LogValue { value, "", unitStr });
	}

	inline void put(std::string key, std::vector<float> value) {
		put(key, LogValue { value, "" });
	}

	void put(std::string key, std::vector<std::vector<float>> value);

	inline void put(std::string key, double value) {
		put(key, LogValue { value, "" });
	}

	inline void put(std::string key, double value, std::string unitStr) {
		put(key, LogValue { value, "", unitStr });
	}

	inline void put(std::string key, std::vector<double> value) {
		put(key, LogValue { value, "" });
	}

	void put(std::string key, std::vector<std::vector<double>> value);

	inline void put(std::string key, std::string value) {
		put(key, LogValue { value, "" });
	}

	inline void put(std::string key, std::vector<std::string> value) {
		put(key, LogValue { value, "" });
	}

	void put(std::string key, std::vector<std::vector<std::string>> value);

	template <typename T>
	requires std::is_enum_v<T>
	inline void put(std::string key, T value) {
		put(key, LogValue { magic_enum::enum_name(value), "" });
	}

	template <typename T>
	requires std::is_enum_v<T>
	void put(std::string key, std::vector<T> value) {
		std::vector < std::string > stringValues;
		for (auto val : value)
			stringValues.emplace_back(magic_enum::enum_name(val));
		put(key, LogValue { stringValues, "" });
	}

	template <typename T>
	requires std::is_enum_v<T>
	void put(std::string key, std::vector<std::vector<T>> value) {
		put(key + "/length", static_cast<long>(value.size()));
		for (int i = 0; i < value.size(); i++)
			put(key + "/" + std::to_string(i), value[i]);
	}

	template <typename U>
	requires units::traits::is_unit_v<U>
	inline void put(std::string key, U value) {
		put(key,
				LogValue { U::base_unit { value }.value(), "",
						U::base_unit { }.abbreviation() });
	}

	inline void put(std::string key, frc::Color value) {
		put(key, value.HexString());
	}

	void put(std::string key, inputs::LoggableInputs &value) {
		if (depth > 100) {
			FRC_ReportWarning(
					"[AdvantageKit] Detected recursive table structure when logging value to field \"{}{}\". using LoggableInputs. Consider revising the table structure or refactoring to avoid recursion.",
					prefix, key);
			return;
		}
		value.toLog(getSubtable(key));
	}

	LogValue get(std::string key) {
		return data.at(prefix + key);
	}

	bool get(std::string key, bool defaultValue) {
		auto value = data.find(prefix + key);
		if (value == data.end())
			return defaultValue;
		return get(key).getBoolean(defaultValue);
	}

	double get(std::string key, double defaultValue) {
		auto value = data.find(prefix + key);
		if (value == data.end())
			return defaultValue;
		return get(key).getDouble(defaultValue);
	}

	std::string get(std::string key, std::string defaultValue) {
		auto value = data.find(prefix + key);
		if (value == data.end())
			return defaultValue;
		return get(key).getString(defaultValue);
	}

private:
	LogTable(std::string prefix, int depth, std::shared_ptr<long> timestamp,
			std::unordered_map<std::string, LogValue> data) : prefix { prefix }, depth {
			depth }, timestamp { timestamp }, data { data } {
	}

	LogTable(std::string prefix, const LogTable &parent) : LogTable { prefix,
			parent.depth + 1, parent.timestamp, parent.data } {
	}

	bool writeAllowed(std::string key, LoggableType type,
			std::string customTypeStr);

	std::string prefix;
	int depth;
	std::shared_ptr<long> timestamp;
	std::unordered_map<std::string, LogValue> data;
};

}
