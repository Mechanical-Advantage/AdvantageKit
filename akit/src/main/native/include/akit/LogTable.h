// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once
#include <string>
#include <unordered_map>
#include <any>
#include <vector>
#include <memory>
#include <unordered_set>

#include <wpi/array.h>
#include <wpi/struct/Struct.h>
#include <magic_enum/magic_enum.hpp>
#include <frc/Errors.h>
#include <frc/util/Color.h>
#include <units/base.h>
#include <units/angle.h>

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
	requires units::traits::is_unit_t_v<U>
	inline void put(std::string key, U value) {
		auto converted = value.template convert<
				units::unit<std::ratio<1>,
						units::traits::base_unit_of<typename U::unit_type>>>();
		put(key, LogValue { converted.value(), "", converted.abbreviation() });
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

	template <typename T>
	requires wpi::StructSerializable<T> && (!std::is_arithmetic_v<T>)
	void put(std::string key, T value) {
		addStructSchema<T>();
		std::array<std::byte, wpi::GetStructSize<T>()> buffer;
		wpi::PackStruct < T > (buffer);
		put(key, LogValue {buffer, wpi::GetStructTypeString<T>()});
	}

	template <typename T>
	requires wpi::StructSerializable<T> && (!std::is_arithmetic_v<T>)
	void put(std::string key, std::initializer_list<T> values) {
		addStructSchema<T>();
		std::vector < std::byte
		> buffer {values.size() * wpi::GetStructSize<T>()};
		int i = 0;
		for (const T &value : values)
		wpi::PackStruct(
				std::span < std::byte
				> (buffer).subspan(i++ * wpi::GetStructSize<T>()),
				value);
		put(key, LogValue {buffer, wpi::GetStructTypeString<T>() + "[]"});
	}

	template <typename T>
	requires wpi::StructSerializable<T> && (!std::is_arithmetic_v<T>)
	void put(std::string key, std::vector<std::vector<T>> value) {
		put(key + "/length", value.size());
		for (int i = 0; i < value.size(); i++)
		put(key + "/" + std::to_string(i), value[i]);
	}

	inline LogValue get(std::string key) {
		return data.at(prefix + key);
	}

	std::vector<std::byte> get(std::string key,
			std::vector<std::byte> defaultValue);
	std::vector<std::vector<std::byte>> get(std::string key,
			std::vector<std::vector<std::byte>> defaultValue);

	bool get(std::string key, bool defaultValue);
	std::vector<bool> get(std::string key, std::vector<bool> defaultValue);
	std::vector<std::vector<bool>> get(std::string key,
			std::vector<std::vector<bool>> defaultValue);

	long get(std::string key, long defaultValue);
	std::vector<long> get(std::string key, std::vector<long> defaultValue);
	std::vector<std::vector<long>> get(std::string key,
			std::vector<std::vector<long>> defaultValue);

	float get(std::string key, float defaultValue);
	std::vector<float> get(std::string key, std::vector<float> defaultValue);
	std::vector<std::vector<float>> get(std::string key,
			std::vector<std::vector<float>> defaultValue);

	double get(std::string key, double defaultValue);
	std::vector<double> get(std::string key, std::vector<double> defaultValue);
	std::vector<std::vector<double>> get(std::string key,
			std::vector<std::vector<double>> defaultValue);

	std::string get(std::string key, std::string defaultValue);
	std::vector<std::string> get(std::string key,
			std::vector<std::string> defaultValue);
	std::vector<std::vector<std::string>> get(std::string key,
			std::vector<std::vector<std::string>> defaultValue);

	template <typename T>
	requires std::is_enum_v<T>
	T get(std::string key, T defaultValue) {
		if (data.contains(prefix + key))
			return magic_enum::enum_cast(
					get(key).getString(magic_enum::enum_name(defaultValue)));
		else
			return defaultValue;
	}

	template <typename T>
	requires std::is_enum_v<T>
	std::vector<T> get(std::string key, std::vector<T> defaultValue) {
		if (data.contains(prefix + key)) {
			std::vector < std::string > names = get(key).getStringArray( { });
			std::vector < T > enums;
			for (const auto &name : names)
				enums.emplace_back(magic_enum::enum_cast < T > (name));
			return enums;
		} else
			return defaultValue;
	}

	template <typename T>
	requires std::is_enum_v<T>
	std::vector<std::vector<T>> get(std::string key,
			std::vector<std::vector<T>> defaultValue) {
		if (data.contains(prefix + key)) {
			long length = get(key + "/length", 0L);
			std::vector < std::vector < T >> values;
			for (int i = 0; i < length; i++)
				values.emplace_back(
						get(key + "/" + std::to_string(i), defaultValue[i]));
			return values;
		} else
			return defaultValue;
	}

	template <typename U>
	requires units::traits::is_unit_t_v<U>
	U get(std::string key, U defaultValue) {
		using BaseUnit = units::unit<std::ratio<1>, units::traits::base_unit_of<typename U::unit_type>>;
		if (data.contains(prefix + key)) {
			auto converted = defaultValue.template convert<BaseUnit>();
			return BaseUnit { get(key).getDouble(converted.value()) };
		} else
			return defaultValue;
	}

	frc::Color get(std::string key, frc::Color defaultValue);

	inputs::LoggableInputs get(std::string key,
			inputs::LoggableInputs defaultValue);

	template <typename T>
	requires wpi::StructSerializable<T> && (!std::is_arithmetic_v<T>)
	T get(std::string key, T defaultValue) {
		if (data.contains(prefix + key))
		return wpi::UnpackStruct<T>(get(key).getRaw());
		else return defaultValue;
	}

	template <typename T>
	requires wpi::StructSerializable<T> && (!std::is_arithmetic_v<T>)
	std::vector<T> get(std::string key, std::vector<T> defaultValue) {
		if (data.contains(prefix + key)) {
			std::vector<std::byte> buffer = get(key).getRaw();
			std::vector<T> structs {buffer.size() / wpi::GetStructSize<T>()};
			for (int i = 0; i < structs.size(); i++)
			wpi::UnpackStructInto(structs.data() + i, std::span<std::byte> {buffer}.subspan(i * wpi::GetStructSize<T>()));
			return structs;
		} else return defaultValue;
	}

	template <typename T>
	requires wpi::StructSerializable<T> && (!std::is_arithmetic_v<T>)
	std::vector<std::vector<T>> get(std::string key, std::vector<std::vector<T>> defaultValue) {
		if (data.contains(prefix + key)) {
			long length = get(key + "/length", 0L);
			std::vector<std::vector<T>> structs;
			for (int i = 0; i < length; i++) {
				structs.emplace_back(get(key + "/" + std::to_string(i), defaultValue[i]));
			}
		} else return defaultValue;
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

	template <typename T>
	requires wpi::StructSerializable<T> && (!std::is_arithmetic_v<T>)
	void addStructSchema() {
		std::string typeString = wpi::GetStructTypeString<T>();
		std::string key = "/.schema/" + typeString;

		if (data.contains(key))
		return;
		std::unordered_set < std::string > seen;
		seen.insert(typeString);

		data.emplace(key, LogValue {wpi::GetStructSchemaBytes<T>(),
					"structschema"});
		wpi::ForEachStructSchema([&](std::string_view typeString, std::string_view schema) {addStructSchema(std::string {typeString}, std::string {schema}, seen);});
	}

	void addStructSchema(std::string typeString, std::string schema,
			std::unordered_set<std::string> &seen);

	std::string prefix;
	int depth;
	std::shared_ptr<long> timestamp;
	std::unordered_map<std::string, LogValue> data;
};

}
