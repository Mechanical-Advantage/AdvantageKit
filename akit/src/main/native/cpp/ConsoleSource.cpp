// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include <fstream>
#include <frc/Errors.h>
#include <vector>
#include <fcntl.h>
#ifdef _WIN32
#include <io.h>
#include <windows.h>
#endif
#include "akit/ConsoleSource.h"

using namespace akit;

SimulatorConsoleSource::SimulatorConsoleSource() {
#ifdef _WIN32
	_pipe(stdoutPipe, 4096, _O_TEXT);
	_pipe(stderrPipe, 4096, _O_TEXT);
	originalCout = _dup(_fileno(stdout));
	originalCerr = _dup(_fileno(stderr));
	_dup2(stdoutPipe[1], _fileno(stdout));
	_dup2(stderrPipe[1], _fileno(stderr));
#else
	pipe(stdoutPipe);
	pipe(stderrPipe);
	originalCout = dup(STDOUT_FILENO);
	originalCerr = dup(STDERR_FILENO);

	dup2(stdoutPipe[1], STDOUT_FILENO);
	dup2(stderrPipe[1], STDERR_FILENO);

	close(stdoutPipe[1]);
	close(stderrPipe[1]);
#endif

	setvbuf(stdout, NULL, _IONBF, 0);
	setvbuf(stderr, NULL, _IONBF, 0);
}

SimulatorConsoleSource::~SimulatorConsoleSource() {
	running = false;
	thread.join();

#ifdef _WIN32
	_dup2(originalCout, _fileno(stdout));
	_dup2(originalCerr, _fileno(stderr));
	_close(originalCout);
	_close(originalCerr);
#else
	dup2(originalCout, STDOUT_FILENO);
	dup2(originalCerr, STDERR_FILENO);

	close(originalCout);
	close(originalCerr);
#endif
}

void SimulatorConsoleSource::Run() {
	char buffer[1024];
	while (running) {
#ifdef _WIN32
		DWORD bytesAvailable = 0;
		HANDLE hStdOut = reinterpret_cast<HANDLE>(_get_osfhandle(stdoutPipe[0]));
		if (PeekNamedPipe(hStdOut, NULL, 0, NULL, &bytesAvailable, NULL) && bytesAvailable > 0) {
			int count = _read(stdoutPipe[0], buffer, sizeof(buffer));
			if (count > 0) _write(originalCout, buffer, count);
		}
		HANDLE hStdErr = reinterpret_cast<HANDLE>(_get_osfhandle(stderrPipe[0]));
		if (PeekNamedPipe(hStdErr, NULL, 0, NULL, &bytesAvailable, NULL) && bytesAvailable > 0) {
			int count = _read(stderrPipe[0], buffer, sizeof(buffer));
			if (count > 0) _write(originalCerr, buffer, count);
		}
#else
#endif

		// int count = read(stdoutPipe[0], buffer, sizeof(buffer));
		// if (count > 0)
		// 	write(originalCout, buffer, count);

		// count = read(stderrPipe[0], buffer, sizeof(buffer));
		// if (count > 0)
		// 	write(originalCerr, buffer, count);
		std::this_thread::sleep_for(std::chrono::milliseconds{5});
	}
}

std::string SimulatorConsoleSource::GetNewData() {
	return "";
	// std::string fullOut = capturedCout.str();
	// std::string newOut = fullOut.substr(coutPos);
	// coutPos = fullOut.size();

	// std::string fullErr = capturedCerr.str();
	// std::string newErr = fullErr.substr(cerrPos);
	// cerrPos = fullErr.size();

	// return newOut + newErr;
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
