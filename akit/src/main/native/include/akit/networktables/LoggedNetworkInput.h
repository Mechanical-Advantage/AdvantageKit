// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once
#include <string>

namespace akit {

namespace nt {

class LoggedNetworkInput {
public:
	virtual void Periodic() = 0;

protected:
	static constexpr std::string_view PREFIX = "NetworkInputs";

	static std::string RemoveSlash(std::string key);
};

}

}
