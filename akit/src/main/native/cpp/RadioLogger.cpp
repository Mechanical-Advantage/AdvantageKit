// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include <regex>
#include <frc/RobotController.h>

#include <httplib.h>

#include "akit/RadioLogger.h"

using namespace akit;

std::optional<frc::Notifier> RadioLogger::notifier;
bool RadioLogger::isConnected = false;
std::string RadioLogger::statusJson;
std::mutex RadioLogger::mutex;

void RadioLogger::Periodic(LogTable &&table) {
	if (!notifier && frc::RobotController::GetTeamNumber() != 0)
		Start();

	{
		std::lock_guard lock { mutex };
		table.Put("Connected", isConnected);
		table.Put("Status", LogTable::LogValue { statusJson, "json" });
	}
}

void RadioLogger::Start() {
	int teamNumber = frc::RobotController::GetTeamNumber();
	std::string url = fmt::format("http://10.{}.{}.1", teamNumber / 100,
			teamNumber % 100);
	notifier = frc::Notifier { [url] {
		httplib::Client request { url };
		request.set_connection_timeout(std::chrono::duration<double> {
				CONNECTION_TIMEOUT.value() });
		request.set_read_timeout(
				std::chrono::duration<double> { READ_TIMEOUT.value() });
		auto response = request.Get("/status");
		std::string responseStr = std::regex_replace(response->body,
				std::regex { "\\s+" }, "");
		{
			std::lock_guard lock { mutex };
			isConnected = responseStr.size() > 0;
			statusJson = responseStr;
		}
	} };
	notifier->SetName("AdvantageKit_RadioLogger");
	notifier->StartPeriodic(REQUEST_PERIOD);
}
