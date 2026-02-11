// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include <string>
#include <regex>
#include <iostream>
#include <filesystem>
#include <fstream>
#include "akit/LogFileUtil.h"

using namespace akit;
namespace fs = std::filesystem;

std::string LogFileUtil::addPathSuffix(std::string path, std::string suffix) {
	size_t dotIndex = path.find_last_of('.');
	if (dotIndex == std::string::npos)
		return path;
	std::string basename = path.substr(0, dotIndex);
	std::string extension = path.substr(dotIndex);
	if (basename.ends_with(suffix))
		return basename + "_2" + extension;
	else if (std::regex_match(basename,
			std::regex { ".+" + suffix + "_[0-9]+$" })) {
		int splitIndex = basename.find_last_of('_');
		int index = std::stoi(basename.substr(splitIndex + 1));
		return basename.substr(0, splitIndex) + "_" + std::to_string(index + 1)
				+ extension;
	} else
		return basename + suffix + extension;
}

std::string LogFileUtil::findReplayLog() {
	std::string envPath = findReplayLogEnvVar();
	if (!envPath.empty()) {
		std::cout << "[AdvantageKit] Replaying log from "
				<< ENVIRONMENT_VARIABLE << " environment variable: \""
				<< envPath << "\"\n";
		return envPath;
	}

	std::string advantageScopeLogPath = findReplayLogAdvantageScope();
	if (!advantageScopeLogPath.empty()) {
		std::cout << "[AdvantageKit] Replaying log from AdvantageScope: \""
				<< advantageScopeLogPath << "\"\n";
		return advantageScopeLogPath;
	}

	std::cout << "No log provided with the " << ENVIRONMENT_VARIABLE
			<< " environment variable or through AdvantageScope. Enter path to file: ";
	std::string filename = findReplayLogUser();
	if (filename[0] == '\'' || filename[0] == '"')
		filename = filename.substr(1, filename.size() - 1);
	return filename;
}

std::string LogFileUtil::findReplayLogEnvVar() {
	return std::getenv(ENVIRONMENT_VARIABLE.data());
}

std::string LogFileUtil::findReplayLogAdvantageScope() {
	fs::path advantageScopeTempPath = fs::temp_directory_path()
			/ ADVANTAGESCOPE_FILENAME;
	std::ifstream file { advantageScopeTempPath };
	std::string advantageScopeLogPath;
	std::getline(file, advantageScopeLogPath);
	return advantageScopeLogPath;
}

std::string LogFileUtil::findReplayLogUser() {
	std::string filename;
	std::getline(std::cin, filename);
	return filename;
}
