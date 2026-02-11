// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once
#include <networktables/NetworkTable.h>
#include <frc/geometry/Pose3d.h>
#include "akit/LogTable.h"

namespace akit {

namespace mech {

class LoggedMechanismObject2d {
public:
	virtual units::meter_t getObject2dRange() = 0;

	virtual units::degree_t getAngle() = 0;

	std::vector<frc::Pose3d> generate3dMechanism(frc::Pose3d seed);

	std::string getName() {
		return name;
	}

protected:
	LoggedMechanismObject2d(std::string name) : name { name } {
	}

	LoggedMechanismObject2d* append(
			std::unique_ptr<LoggedMechanismObject2d> object);

	virtual void updateEntries(std::shared_ptr<nt::NetworkTable> table) = 0;

private:
	void logOutput(LogTable &&table);

	void update(std::shared_ptr<nt::NetworkTable> table);

	std::string name;
	std::shared_ptr<nt::NetworkTable> table;
	std::unordered_map<std::string, std::unique_ptr<LoggedMechanismObject2d>> objects;
};

}

}
