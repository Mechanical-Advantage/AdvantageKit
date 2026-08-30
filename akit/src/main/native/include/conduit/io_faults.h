// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once

#include <wpi/util/struct/Struct.hpp>

// TODO: Check this struct definition against the real struct published by Systemcore
struct IOFaults {
	bool brownout = false;
	int32_t brownoutCount = 0;
	bool display = false;
	int32_t displayCount = 0;
	bool imu = false;
	int32_t imuCount = 0;
	bool io = false;
	int32_t ioCount = 0;
	bool rsl = false;
	int32_t rslCount = 0;
	bool usb = false;
	int32_t usbCount = 0;
};

template <>
struct wpi::util::Struct<IOFaults> {
	static constexpr std::string_view GetTypeName() {return "IOFaults";}
	static constexpr size_t GetSize() {return 30;}
	static constexpr std::string_view GetSchema() {
		return "bool brownout;int32 brownoutCount;bool display;int32 displayCount;bool imu;int32 imuCount;bool io;int32 ioCount;bool rsl;int32 rslCount;bool usb;int32 usbCount";
	}

	static IOFaults Unpack(std::span<const uint8_t> data) {
		return {
			.brownout = wpi::util::UnpackStruct<bool, 0>(data),
			.brownoutCount = wpi::util::UnpackStruct<int32_t, 1>(data),
			.display = wpi::util::UnpackStruct<bool, 5>(data),
			.displayCount = wpi::util::UnpackStruct<int32_t, 6>(data),
			.imu = wpi::util::UnpackStruct<bool, 10>(data),
			.imuCount = wpi::util::UnpackStruct<int32_t, 11>(data),
			.io = wpi::util::UnpackStruct<bool, 15>(data),
			.ioCount = wpi::util::UnpackStruct<int32_t, 16>(data),
			.rsl = wpi::util::UnpackStruct<bool, 20>(data),
			.rslCount = wpi::util::UnpackStruct<int32_t, 21>(data),
			.usb = wpi::util::UnpackStruct<bool, 25>(data),
			.usbCount = wpi::util::UnpackStruct<int32_t, 26>(data),
		};
	}

	static void Pack(std::span<uint8_t> data, const IOFaults &value) {
		wpi::util::PackStruct<0>(data, value.brownout);
		wpi::util::PackStruct<1>(data, value.brownoutCount);
		wpi::util::PackStruct<5>(data, value.display);
		wpi::util::PackStruct<6>(data, value.displayCount);
		wpi::util::PackStruct<10>(data, value.imu);
		wpi::util::PackStruct<11>(data, value.imuCount);
		wpi::util::PackStruct<15>(data, value.io);
		wpi::util::PackStruct<16>(data, value.ioCount);
		wpi::util::PackStruct<20>(data, value.rsl);
		wpi::util::PackStruct<21>(data, value.rslCount);
		wpi::util::PackStruct<25>(data, value.usb);
		wpi::util::PackStruct<26>(data, value.usbCount);
	}
};

static_assert(wpi::util::StructSerializable<IOFaults>);
