// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include <random>
#include <iomanip>
#include <iostream>
#include <fstream>
#include <chrono>
#include <filesystem>
#include <frc/DriverStation.h>
#include <frc/RobotController.h>
#include <wpi/DataLogWriter.h>
#include <frc/Timer.h>
#include "akit/wpilog/WPILOGWriter.h"
#include "akit/wpilog/WPILOGConstants.h"
#include "akit/Logger.h"

using namespace akit::wpilog;
namespace fs = std::filesystem;

WPILOGWriter::WPILOGWriter(std::string_view path,
		AdvantageScopeOpenBehavior openBehavior) : openBehavior { openBehavior } {
	std::mt19937 gen { std::random_device { }() };
	std::uniform_int_distribution dis { 0, 0xFFFF };

	std::stringstream ss;
	for (int i = 0; i < 4; i++)
		ss << std::hex << std::setw(4) << std::setfill('0') << dis(gen);

	randomIdentifier = ss.str();

	fs::path p { path };
	if (p.extension() == ".wpilog") {
		folder = p.parent_path();
		filename = p.filename();
		autoRename = false;
	} else {
		folder = p;
		filename = "akit_" + randomIdentifier + ".wpilog";
		autoRename = true;
	}
}

void WPILOGWriter::Start() {
	fs::create_directories (folder);
	fs::remove (filename);
	std::cout << "[AdvantageKit] Logging to \"" << folder / filename << "\"\n";
	std::error_code ec;
	log = std::make_unique < wpi::log::DataLogWriter
			> ((folder / filename).string(), ec, WPILOGConstants::EXTRA_HEADER);
	if (ec) {
		FRC_ReportError(frc::err::Error,
				"[AdvantageKit] Failed to open output log file.");
		return;
	}
	isOpen = true;
	timestampID =
			log->Start(TIMESTAMP_KEY,
					LogTable::WPILOG_TYPES[static_cast<int>(LogTable::LoggableType::Integer)]);
}

void WPILOGWriter::End() {
	log.reset();

	bool shouldOpen = false;
	switch (openBehavior) {
	case AdvantageScopeOpenBehavior::ALWAYS:
		shouldOpen = frc::RobotBase::IsSimulation();
		break;
	case AdvantageScopeOpenBehavior::AUTO:
		shouldOpen = frc::RobotBase::IsSimulation()
				&& Logger::HasReplaySource();
		break;
	default:
		break;
	}

	if (shouldOpen) {
		fs::path fullLogPath = fs::absolute(folder / filename);
		fs::path tempPath = fs::temp_directory_path() / ADVANTAGESCOPE_FILENAME;
		std::ofstream writer { tempPath };
		writer << fullLogPath;
		std::cout << "[AdvantageKit] Log sent to AdvantageScope.\n";
	}
}

void WPILOGWriter::PutTable(LogTable &table) {
	if (!isOpen)
		return;

	if (autoRename) {
		if (!logDate) {
			if ((table.Get("DriverStation/DSAttached", false)
					&& table.Get("SystemStats/SystemTimeValid", false))
					|| frc::RobotBase::IsSimulation())
				dsAttachedTime = frc::Timer::GetFPGATimestamp();
			else if (frc::Timer::GetFPGATimestamp() - *dsAttachedTime
					> TIMESTAMP_UPDATE_DELAY
					|| frc::RobotBase::IsSimulation()) {
				std::time_t now = std::time(nullptr);
				logDate = *std::localtime(&now);
			}
		} else
			dsAttachedTime.reset();

		frc::DriverStation::MatchType matchType = frc::DriverStation::kNone;
		switch (table.Get("DriverStation/MatchType", 0)) {
		case 1:
			matchType = frc::DriverStation::kPractice;
			break;
		case 2:
			matchType = frc::DriverStation::kQualification;
			break;
		case 3:
			matchType = frc::DriverStation::kElimination;
			break;
		}

		if (logMatchText.empty() && matchType != frc::DriverStation::kNone) {
			switch (matchType) {
			case frc::DriverStation::kPractice:
				logMatchText = "p";
				break;
			case frc::DriverStation::kQualification:
				logMatchText = "q";
				break;
			case frc::DriverStation::kElimination:
				logMatchText = "e";
				break;
			default:
				break;
			}
			logMatchText += std::to_string(
					table.Get("DriverStation/MatchNumber", 0));
		}

		std::stringstream newFilenameBuilder;
		newFilenameBuilder << "akit_";
		if (!logDate)
			newFilenameBuilder << randomIdentifier;
		else
			newFilenameBuilder
					<< std::put_time(&(*logDate), TIME_FORMATTER.data());

		std::string eventName = table.Get("DriverStation/EventName",
				std::string { "" });
		std::transform(eventName.begin(), eventName.end(), eventName.begin(),
				[](unsigned char c) {
					return std::tolower(c);
				});
		if (!eventName.empty())
			newFilenameBuilder << "_" << eventName;
		if (!logMatchText.empty())
			newFilenameBuilder << "_" << logMatchText;
		newFilenameBuilder << ".wpilog";
		fs::path newFilename = newFilenameBuilder.str();
		if (newFilename != filename) {
			fs::path logPath = folder / newFilename;
			std::cout << "[AdvantageKit] Renaming log to \"" << logPath << "\"";

			fs::rename(folder / filename, folder / newFilename);
			filename = newFilename;
		}
	}

	log->AppendInteger(timestampID,
			table.GetTimestamp().convert<units::microsecond>().value(),
			table.GetTimestamp().convert<units::microsecond>().value());

	auto newMap = table.GetAll(false);
	auto oldMap = lastTable.GetAll(false);

	for (auto field : newMap) {
		LogTable::LoggableType type = field.second.type;
		std::string unit = field.second.unitStr;
		bool appendData = false;
		if (!entryIDs.contains(field.first)) {
			std::string metadata {
					unit.empty() ?
							WPILOGConstants::ENTRY_METADATA :
							GetUnitMetadata(unit) };
			entryIDs[field.first] = log->Start(field.first,
					LogTable::WPILOG_TYPES[static_cast<int>(field.second.type)],
					metadata);
			entryTypes[field.first] = type;
			if (!unit.empty())
				entryUnits[field.first] = unit;
			appendData = true;
		} else if (field.second != oldMap.at(field.first))
			appendData = true;

		if (appendData) {
			int id = entryIDs[field.first];

			if (!unit.empty() && unit != entryUnits[field.first]) {
				log->SetMetadata(id, GetUnitMetadata(unit),
						table.GetTimestamp().convert<units::microsecond>().value());
				entryUnits[field.first] = unit;
			}

			size_t timestamp =
					table.GetTimestamp().convert<units::microsecond>().value();
			switch (field.second.type) {
			case LogTable::LoggableType::Raw: {
				auto raw = field.second.GetRaw();
				std::vector < uint8_t > data;
				std::transform(raw.begin(), raw.end(), data.begin(),
						[](std::byte b) {
							return static_cast<unsigned char>(b);
						});
				log->AppendRaw(id, data, timestamp);
				break;
			}
			case LogTable::LoggableType::Boolean:
				log->AppendBoolean(id, field.second.GetBoolean(), timestamp);
				break;
			case LogTable::LoggableType::Integer:
				log->AppendInteger(id, field.second.GetInteger(), timestamp);
				break;
			case LogTable::LoggableType::Float:
				log->AppendFloat(id, field.second.GetFloat(), timestamp);
				break;
			case LogTable::LoggableType::Double:
				log->AppendDouble(id, field.second.GetDouble(), timestamp);
				break;
			case LogTable::LoggableType::String:
				log->AppendString(id, field.second.GetString(), timestamp);
				break;
			case LogTable::LoggableType::BooleanArray: {
				auto arr = field.second.GetBooleanArray();
				log->AppendBooleanArray(id, std::vector<uint8_t> { arr.begin(),
						arr.end() }, timestamp);
				break;
			}
			case LogTable::LoggableType::IntegerArray: {
				auto arr = field.second.GetIntegerArray();
				log->AppendIntegerArray(id, std::vector<int64_t> { arr.begin(),
						arr.end() }, timestamp);
				break;
			}
			case LogTable::LoggableType::FloatArray:
				log->AppendFloatArray(id, field.second.GetFloatArray(),
						timestamp);
				break;
			case LogTable::LoggableType::DoubleArray:
				log->AppendDoubleArray(id, field.second.GetDoubleArray(),
						timestamp);
				break;
			case LogTable::LoggableType::StringArray:
				log->AppendStringArray(id, field.second.GetStringArray(),
						timestamp);
				break;
			}
		}
	}

	log->Flush();
	lastTable = table;
}

std::string WPILOGWriter::GetUnitMetadata(std::string_view unit) {
	std::string metadata { WPILOGConstants::ENTRY_METADATA_UNITS };
	metadata.replace(metadata.find("$UNITSTR"), 8, unit);
	return metadata;
}
