// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include "akit/networktables/LoggedNetworkInput.h"

using namespace akit::nt;

std::string LoggedNetworkInput::RemoveSlash(std::string key) {
	if (key.starts_with('/'))
		return key.substr(1);
	return key;
}
