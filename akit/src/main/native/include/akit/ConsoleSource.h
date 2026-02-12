// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once
#include <string>
#include <thread>
#include <queue>
#include <sstream>
#include <iostream>
#include <mutex>

namespace akit {

class ConsoleSource {
public:
	virtual ~ConsoleSource() = default;
	virtual std::string getNewData() = 0;
};

class SimulatorConsoleSource: public ConsoleSource {
public:
	SimulatorConsoleSource();
	~SimulatorConsoleSource() override;

	std::string getNewData() override;

private:
	class SplitBuffer: public std::streambuf {
	public:
		SplitBuffer(std::streambuf *original, std::ostringstream &capture) : original {
				original }, capture { capture } {
		}

	protected:
		int overflow(int c) override;

	private:
		std::streambuf *original;
		std::ostringstream &capture;
	};

	std::streambuf *originalCout;
	std::streambuf *originalCerr;

	std::ostringstream capturedCout;
	std::ostringstream capturedCerr;

	SplitBuffer splitCout { originalCout, capturedCout };
	SplitBuffer splitCerr { originalCerr, capturedCerr };

	size_t coutPos = 0;
	size_t cerrPos = 0;
};

class RoboRIOConsoleSource: public ConsoleSource {
public:
	~RoboRIOConsoleSource() override;

	std::string getNewData() override;

protected:
	virtual std::string getFilePath() {
		return "/home/lvuser/FRC_UserProgram.log";
	}

private:
	void run();

	std::atomic<bool> running = true;
	std::thread thread { &RoboRIOConsoleSource::run, this };
	std::mutex mutex;
	std::queue<std::string> queue;
};

}
