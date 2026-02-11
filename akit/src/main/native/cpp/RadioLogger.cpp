// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include <regex>
#include <frc/RobotController.h>
#include <HTTPRequest.hpp>
#include "akit/RadioLogger.h"

using namespace akit;

std::optional<frc::Notifier> RadioLogger::notifier;
bool RadioLogger::isConnected = false;
std::string RadioLogger::statusJson;

void RadioLogger::periodic(LogTable &&table) {
	if (!notifier && frc::RobotController::GetTeamNumber() != 0)
		start();

	table.put("Connected", isConnected);
	table.put("Status", LogTable::LogValue { statusJson, "json" });
}

void RadioLogger::start() {
	int teamNumber = frc::RobotController::GetTeamNumber();
	std::string url = fmt::format("http://10.{}.{}.1/status", teamNumber / 100,
			teamNumber % 100);
	notifier = frc::Notifier { [url] {
		http::Request request { url };
		auto response = request.send("GET", "", { }, std::chrono::milliseconds {
				static_cast<int>(TIMEOUT.value()) });
		std::string responseStr = std::regex_replace(
				std::string { response.body.begin(), response.body.end() },
				std::regex { "\\s+" }, "");
		isConnected = responseStr.size() > 0;
		statusJson = responseStr;
	} };
	notifier->SetName("AdvantageKit_RadioLogger");
	notifier->StartPeriodic(REQUEST_PERIOD);
}
