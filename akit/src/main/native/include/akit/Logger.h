// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once
#include "akit/LogTable.h"
#include "akit/ConsoleSource.h"
#include "akit/networktables/LoggedNetworkInput.h"
#include "akit/LogReplaySource.h"
#include "akit/ReceiverThread.h"
#include "akit/mechanism/LoggedMechanism2d.h"

namespace akit {

class Logger {
public:
	static void setReplaySource(std::unique_ptr<LogReplaySource> replaySource);
	static void addDataReceiver(std::unique_ptr<LogDataReceiver> dataReceiver);
	static void registerDashboardInput(nt::LoggedNetworkInput&);
	// static void registerURCL();
	static void recordMetadata(std::string key, std::string value);
	static void disableConsoleCapture() {
		enableConsole = false;
	}
	static bool hasReplaySource() {
		return static_cast<bool>(replaySource);
	}
	static void start();
	static void end();
	static void periodicBeforeUser();
	static void periodicAfterUser(units::millisecond_t userCodeLength,
			units::millisecond_t periodicBeforeLength,
			std::string extraConsoleData = "");

	class AdvancedHooks {
	public:
		AdvancedHooks() = delete;
		static void disableRobotBaseCheck() {
			checkRobotBase = false;
		}
		static void invokePeriodicBeforeUser() {
			periodicBeforeUser();
		}
		static void invokePeriodicAfterUser(units::millisecond_t userCodeLength,
				units::millisecond_t periodicBeforeLength,
				std::string extraConsoleData = "") {
			periodicAfterUser(userCodeLength, periodicBeforeLength,
					extraConsoleData);
		}
		static void setConsoleSource(std::unique_ptr<ConsoleSource> console) {
			Logger::console = std::move(console);
		}
	};

	static bool getReceiverQueueFault() {
		return receiverQueueFault;
	}
	static units::second_t getTimestamp();
	static void runEveryN(size_t n, std::function<void()> function);
	static void processInputs(std::string key, inputs::LoggableInputs &inputs);

	static void recordOutput(std::string key, std::vector<std::byte> value);
	static void recordOutput(std::string key,
			std::vector<std::vector<std::byte>> value);
	static void recordOutput(std::string key, bool value);
	static void recordOutput(std::string key, std::function<bool()> value);
	static void recordOutput(std::string key, std::vector<bool> value);
	static void recordOutput(std::string key,
			std::vector<std::vector<bool>> value);
	static void recordOutput(std::string key, long value);
	static void recordOutput(std::string key, std::function<long()> value);
	static void recordOutput(std::string key, std::vector<long> value);
	static void recordOutput(std::string key,
			std::vector<std::vector<long>> value);
	static void recordOutput(std::string key, float value);
	static void recordOutput(std::string key, std::function<float()> value);
	static void recordOutput(std::string key, std::vector<float> value);
	static void recordOutput(std::string key,
			std::vector<std::vector<float>> value);
	static void recordOutput(std::string key, double value);
	static void recordOutput(std::string key, std::function<double()> value);
	static void recordOutput(std::string key, std::vector<double> value);
	static void recordOutput(std::string key,
			std::vector<std::vector<double>> value);
	static void recordOutput(std::string key, std::string value);
	static void recordOutput(std::string key, std::vector<std::string> value);
	static void recordOutput(std::string key,
			std::vector<std::vector<std::string>> value);
	template <typename E>
	requires std::is_enum_v<E>
	static void recordOutput(std::string key, E value) {
		if (running)
			outputTable->put(key, value);
	}
	template <typename E>
	requires std::is_enum_v<E>
	static void recordOutput(std::string key, std::vector<E> value) {
		if (running)
			outputTable->put(key, value);
	}
	template <typename E>
	requires std::is_enum_v<E>
	static void recordOutput(std::string key,
			std::vector<std::vector<E>> value) {
		if (running)
			outputTable->put(key, value);
	}
	template <typename U>
	requires units::traits::is_unit_t_v<U>
	static void recordOutput(std::string key, U value) {
		if (running)
			outputTable->put(key, value.value(), value.name());
	}
	template <typename S>
	requires wpi::StructSerializable<S> && (!std::is_arithmetic_v<S>)
	static void recordOutput(std::string key, S value) {
		if (running) outputTable->put(key, value);
	}template <typename S>
	requires wpi::StructSerializable<S> && (!std::is_arithmetic_v<S>)
	static void recordOutput(std::string key, std::vector<S> value) {
		if (running) outputTable->put(key, value);
	}template <typename S>
	requires wpi::StructSerializable<S> && (!std::is_arithmetic_v<S>)
	static void recordOutput(std::string key, std::vector<std::vector<S>> value) {
		if (running) outputTable->put(key, value);
	}
	static void recordOutput(std::string key, mech::LoggedMechanism2d &value);

private:
	static constexpr int RECEIVER_QUEUE_CAPACITY = 500;

	static bool running;
	static long cycleCount;
	static LogTable entry;
	static std::mutex entryMutex;
	static std::optional<LogTable> outputTable;
	static std::unordered_map<std::string, std::string> metadata;
	static std::unique_ptr<ConsoleSource> console;
	static std::vector<nt::LoggedNetworkInput*> dashboardInputs;
	// urclSupplier
	static bool enableConsole;
	static bool checkRobotBase;

	static std::unique_ptr<LogReplaySource> replaySource;
	static moodycamel::BlockingReaderWriterQueue<LogTable> receiverQueue;
	static std::unique_ptr<ReceiverThread> receiverThread;
	static bool receiverQueueFault;
};

}
