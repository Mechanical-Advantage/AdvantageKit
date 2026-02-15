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
	virtual units::meter_t GetObject2dRange() = 0;

	virtual units::degree_t GetAngle() = 0;

	std::vector<frc::Pose3d> Generate3dMechanism(frc::Pose3d seed);

	std::string GetName() {
		return name;
	}

	void Update(std::shared_ptr<::nt::NetworkTable> table);

	virtual void LogOutput(LogTable &&table);

protected:
	LoggedMechanismObject2d(std::string name) : name { name } {
	}

	LoggedMechanismObject2d* Append(
			std::unique_ptr<LoggedMechanismObject2d> object);

	virtual void UpdateEntries(std::shared_ptr<::nt::NetworkTable> table) = 0;

private:
	std::string name;
	std::shared_ptr<::nt::NetworkTable> table;
	std::unordered_map<std::string, std::unique_ptr<LoggedMechanismObject2d>> objects;
	std::mutex mutex;
};

}

}
