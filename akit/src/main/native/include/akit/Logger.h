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
	static void SetReplaySource(std::unique_ptr<LogReplaySource> replaySource);
	static void AddDataReceiver(std::unique_ptr<LogDataReceiver> dataReceiver);
	static void RegisterDashboardInput(nt::LoggedNetworkInput&);
	// static void registerURCL();
	static void RecordMetadata(std::string key, std::string value);
	static void DisableConsoleCapture() {
		enableConsole = false;
	}
	static bool HasReplaySource() {
		return static_cast<bool>(replaySource);
	}
	static void Start();
	static void End();
	static void PeriodicBeforeUser();
	static void PeriodicAfterUser(units::millisecond_t userCodeLength,
			units::millisecond_t periodicBeforeLength,
			std::string extraConsoleData = "");

	class AdvancedHooks {
	public:
		AdvancedHooks() = delete;
		static void DisableRobotBaseCheck() {
			checkRobotBase = false;
		}
		static void InvokePeriodicBeforeUser() {
			PeriodicBeforeUser();
		}
		static void InvokePeriodicAfterUser(units::millisecond_t userCodeLength,
				units::millisecond_t periodicBeforeLength,
				std::string extraConsoleData = "") {
			PeriodicAfterUser(userCodeLength, periodicBeforeLength,
					extraConsoleData);
		}
		static void SetConsoleSource(std::unique_ptr<ConsoleSource> console) {
			Logger::console = std::move(console);
		}
	};

	static bool GetReceiverQueueFault() {
		return receiverQueueFault;
	}
	static units::second_t GetTimestamp();
	static void RunEveryN(size_t n, std::function<void()> function);
	static void ProcessInputs(std::string key, inputs::LoggableInputs &inputs);

	template<typename T>
	inline static void RecordOutput(std::string key, T value) {
		if (running)
			outputTable->Put(key, value);
	}
	template<typename T>
	inline static void RecordOutput(std::string key, std::function<T()> value) {
		recordOutput(value());
	}
	static void RecordOutput(std::string key, mech::LoggedMechanism2d &value);

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
	static moodycamel::BlockingConcurrentQueue<LogTable> receiverQueue;
	static std::unique_ptr<ReceiverThread> receiverThread;
	static bool receiverQueueFault;
};

}
