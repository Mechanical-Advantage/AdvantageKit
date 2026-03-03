// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once
#include <string>
#include <thread>
#include <sstream>
#include <iostream>
#include <mutex>
#include <readerwritercircularbuffer.h>

namespace akit {

class ConsoleSource {
public:
	virtual ~ConsoleSource() = default;
	virtual std::string GetNewData() = 0;
};

class SimulatorConsoleSource: public ConsoleSource {
public:
	SimulatorConsoleSource();
	~SimulatorConsoleSource() override;

	std::string GetNewData() override;

private:
	void Run();

	std::atomic<bool> running = true;
	std::thread thread{&SimulatorConsoleSource::Run, this};

	int stdoutPipe[2];
	int stderrPipe[2];

	int originalCout;
	int originalCerr;

	size_t coutPos = 0;
	size_t cerrPos = 0;
};

class RoboRIOConsoleSource: public ConsoleSource {
public:
	~RoboRIOConsoleSource() override;

	std::string GetNewData() override;

protected:
	virtual std::string GetFilePath() {
		return "/home/lvuser/FRC_UserProgram.log";
	}

private:
	void Run();

	std::atomic<bool> running = true;
	std::thread thread { &RoboRIOConsoleSource::Run, this };
	std::mutex mutex;
	moodycamel::BlockingReaderWriterCircularBuffer<std::string> queue { 100 };
};

}
