// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once

#include <wpi/util/struct/Struct.hpp>

struct IOFaults {
	bool brownout = false;
	bool io = false;
	bool rsl = false;
	bool usb = false;
	bool display = false;
	bool imu = false;
	int32_t brownoutCount = 0;
	int32_t ioCount = 0;
	int32_t rslCount = 0;
	int32_t usbCount = 0;
	int32_t displayCount = 0;
	int32_t imuCount = 0;
};

template <>
struct wpi::util::Struct<IOFaults> {
	static constexpr std::string_view GetTypeName() {return "IOFaults";}
	static constexpr size_t GetSize() {return 30;}
	static constexpr std::string_view GetSchema() {
		return "bool brownout;bool io;bool rsl;bool usb;bool display;bool imu;int32 brownoutCount;int32 ioCount;int32 rslCount;int32 usbCount;int32 displayCount;int32 imuCount";
	}

	static IOFaults Unpack(std::span<const uint8_t> data) {
		return {
			.brownout = wpi::util::UnpackStruct<bool, 0>(data),
			.io = wpi::util::UnpackStruct<bool, 1>(data),
			.rsl = wpi::util::UnpackStruct<bool, 2>(data),
			.usb = wpi::util::UnpackStruct<bool, 3>(data),
			.display = wpi::util::UnpackStruct<bool, 4>(data),
			.imu = wpi::util::UnpackStruct<bool, 5>(data),
			.brownoutCount = wpi::util::UnpackStruct<int32_t, 6>(data),
			.ioCount = wpi::util::UnpackStruct<int32_t, 10>(data),
			.rslCount = wpi::util::UnpackStruct<int32_t, 14>(data),
			.usbCount = wpi::util::UnpackStruct<int32_t, 18>(data),
			.displayCount = wpi::util::UnpackStruct<int32_t, 22>(data),
			.imuCount = wpi::util::UnpackStruct<int32_t, 26>(data),
		};
	}

	static void Pack(std::span<uint8_t> data, const IOFaults &value) {
		wpi::util::PackStruct<0>(data, value.brownout);
		wpi::util::PackStruct<1>(data, value.io);
		wpi::util::PackStruct<2>(data, value.rsl);
		wpi::util::PackStruct<3>(data, value.usb);
		wpi::util::PackStruct<4>(data, value.display);
		wpi::util::PackStruct<5>(data, value.imu);
		wpi::util::PackStruct<6>(data, value.brownoutCount);
		wpi::util::PackStruct<10>(data, value.ioCount);
		wpi::util::PackStruct<14>(data, value.rslCount);
		wpi::util::PackStruct<18>(data, value.usbCount);
		wpi::util::PackStruct<22>(data, value.displayCount);
		wpi::util::PackStruct<26>(data, value.imuCount);
	}
};

static_assert(wpi::util::StructSerializable<IOFaults>);

struct CanBusInfoEntry {
	double nominalMbps = 0.0;
	double dataMbps = 0.0;
	double samplePoint = 0.0;
	double dataSamplePoint = 0.0;
	bool fd = false;
	bool avail = false;
	bool up = false;
};

template <>
struct wpi::util::Struct<CanBusInfoEntry> {
	static constexpr std::string_view GetTypeName() {return "CanBusInfoEntry";}
	static constexpr size_t GetSize() {return 35;}
	static constexpr std::string_view GetSchema() {
		return "double nominalMbps;double dataMbps;double samplePoint;double dataSamplePoint;bool fd;bool avail;bool up";
	}

	static CanBusInfoEntry Unpack(std::span<const uint8_t> data) {
		return {
			.nominalMbps = wpi::util::UnpackStruct<double, 0>(data),
			.dataMbps = wpi::util::UnpackStruct<double, 8>(data),
			.samplePoint = wpi::util::UnpackStruct<double, 16>(data),
			.dataSamplePoint = wpi::util::UnpackStruct<double, 24>(data),
			.fd = wpi::util::UnpackStruct<bool, 32>(data),
			.avail = wpi::util::UnpackStruct<bool, 33>(data),
			.up = wpi::util::UnpackStruct<bool, 34>(data),
		};
	}

	static void Pack(std::span<uint8_t> data, const CanBusInfoEntry &value) {
		wpi::util::PackStruct<0>(data, value.nominalMbps);
		wpi::util::PackStruct<8>(data, value.dataMbps);
		wpi::util::PackStruct<16>(data, value.samplePoint);
		wpi::util::PackStruct<24>(data, value.dataSamplePoint);
		wpi::util::PackStruct<32>(data, value.fd);
		wpi::util::PackStruct<33>(data, value.avail);
		wpi::util::PackStruct<34>(data, value.up);
	}
};

static_assert(wpi::util::StructSerializable<CanBusInfoEntry>);
