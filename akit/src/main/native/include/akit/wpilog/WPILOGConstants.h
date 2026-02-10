// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once
#include <string_view>

namespace akit {

namespace wpilog {

class WPILOGConstants {
public:
	static constexpr std::string_view EXTRA_HEADER = "AdvantageKit";
	static constexpr std::string_view EXTRA_METADATA =
			"{\"source\":\"AdvantageKit\"}";
	static constexpr std::string_view ENTRY_METADATA_UNITS =
			"{\"source\":\"AdvantageKit\",\"unit\":\"$UNITSTR\"}";
};

}

}
