// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include "akit/AlertLogger.h"

using namespace akit;

std::unordered_map<std::string, nt::StringArraySubscriber> AlertLogger::errorSubscribers;
std::unordered_map<std::string, nt::StringArraySubscriber> AlertLogger::warningSubscribers;
std::unordered_map<std::string, nt::StringArraySubscriber> AlertLogger::infoSubscribers;

void AlertLogger::periodic() {
}
