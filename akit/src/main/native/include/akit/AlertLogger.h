// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once
#include <networktables/StringArrayTopic.h>

namespace akit {

class AlertLogger {
public:
	static void periodic();

private:
	static std::unordered_map<std::string, nt::StringArraySubscriber> errorSubscribers;
	static std::unordered_map<std::string, nt::StringArraySubscriber> warningSubscribers;
	static std::unordered_map<std::string, nt::StringArraySubscriber> infoSubscribers;
};

}
