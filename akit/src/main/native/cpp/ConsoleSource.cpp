// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include <fstream>
#include <frc/Errors.h>
#include <vector>
#include "akit/ConsoleSource.h"

using namespace akit;

SimulatorConsoleSource::SimulatorConsoleSource() : originalCout {
		std::cout.rdbuf(&splitCout) }, originalCerr { std::cerr.rdbuf(
		&splitCerr) } {
}

SimulatorConsoleSource::~SimulatorConsoleSource() {
	std::cout.rdbuf(originalCout);
	std::cerr.rdbuf(originalCerr);
}

std::string SimulatorConsoleSource::GetNewData() {
	std::string fullOut = capturedCout.str();
	std::string newOut = fullOut.substr(coutPos);
	coutPos = fullOut.size();

	std::string fullErr = capturedCerr.str();
	std::string newErr = fullErr.substr(coutPos);
	cerrPos = fullErr.size();

	return newOut + newErr;
}

int SimulatorConsoleSource::SplitBuffer::overflow(int c) {
	if (c == EOF)
		return !EOF;
	original->sputc(c);
	capture.put(static_cast<char>(c));
	return c;
}

RoboRIOConsoleSource::~RoboRIOConsoleSource() {
	running = false;
	thread.join();
}

std::string RoboRIOConsoleSource::GetNewData() {
	std::vector < std::string > lines;
	std::string line;
	while (queue.try_dequeue(line))
		lines.push_back(line);

	std::ostringstream out;
	for (size_t i = 0; i < lines.size(); i++) {
		out << lines[i];
		if (i + 1 < lines.size())
			out << '\n';
	}
	return out.str();
}

void RoboRIOConsoleSource::Run() {
	std::ifstream file { GetFilePath() };
	if (!file.is_open()) {
		FRC_ReportError(frc::err::Error,
				"[AdvantageKit] Failed to open console file \"{}\", disabling console capture.",
				GetFilePath());
		return;
	}

	std::string buffer;
	std::string line;

	while (running) {
		while (std::getline(file, line)) {
			std::lock_guard < std::mutex > lock { mutex };
			std::string consumer;
			while (!queue.try_enqueue(line))
				queue.try_dequeue(consumer);
		}

		std::this_thread::sleep_for(std::chrono::milliseconds { 20 });
	}
}
