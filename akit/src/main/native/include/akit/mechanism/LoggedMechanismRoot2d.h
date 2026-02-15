// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once
#include <networktables/DoubleTopic.h>
#include "akit/mechanism/LoggedMechanismObject2d.h"

namespace akit {

namespace mech {

class LoggedMechanismRoot2d {
public:
	LoggedMechanismRoot2d(std::string name, units::meter_t x, units::meter_t y) : name {
			name }, x { x }, y { y } {
	}

	LoggedMechanismObject2d* Append(
			std::unique_ptr<LoggedMechanismObject2d> object);

	void SetPosition(units::meter_t x, units::meter_t y);

	std::string GetName() {
		return name;
	}

	std::vector<frc::Pose3d> Generate3dMechanism();

	void Update(std::shared_ptr<::nt::NetworkTable> table);

	void LogOutput(LogTable &&table);

private:
	void Flush();

	std::string name;
	std::shared_ptr<::nt::NetworkTable> table;
	std::unordered_map<std::string, std::unique_ptr<LoggedMechanismObject2d>> objects;
	units::meter_t x;
	::nt::DoublePublisher xPub;
	units::meter_t y;
	::nt::DoublePublisher yPub;
	std::mutex mutex;
};

}

}
