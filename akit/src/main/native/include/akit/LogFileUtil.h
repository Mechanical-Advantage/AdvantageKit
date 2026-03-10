// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once
#include <string_view>

namespace akit {

class LogFileUtil {
public:
	static std::string AddPathSuffix(std::string path, std::string suffix);

	static std::string FindReplayLog();

	static std::string FindReplayLogEnvVar();

	static std::string FindReplayLogAdvantageScope();

	static std::string FindReplayLogUser();

private:
	static constexpr std::string_view ENVIRONMENT_VARIABLE = "AKIT_LOG_PATH";
	static constexpr std::string_view ADVANTAGESCOPE_FILENAME =
			"akit-log-path.txt";
};

}
