// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include "akit/mechanism/LoggedMechanismObject2d.h"

using namespace akit::mech;

LoggedMechanismObject2d* LoggedMechanismObject2d::append(
		std::unique_ptr<LoggedMechanismObject2d> object) {
	if (objects.contains(object->getName()))
		throw std::runtime_error { "Mechanism object names must be unique!" };

	auto obj = object.get();
	objects[object->getName()] = std::move(object);

	if (table)
		obj->update(table->GetSubTable(obj->getName()));
	return obj;
}

void LoggedMechanismObject2d::update(std::shared_ptr<nt::NetworkTable> table) {
	this->table = table;
	updateEntries(this->table);
	for (auto &obj : objects)
		obj.second->update(table->GetSubTable(obj.second->name));
}

void LoggedMechanismObject2d::logOutput(LogTable &&table) {
	for (auto &obj : objects)
		obj.second->logOutput(table.getSubtable(obj.second->name));
}

std::vector<frc::Pose3d> LoggedMechanismObject2d::generate3dMechanism(
		frc::Pose3d seed) {
	std::vector < frc::Pose3d > poses;

	for (auto &obj : objects) {
		frc::Rotation3d newRotation { 0_deg, -obj.second->getAngle(), 0_deg };
		newRotation = seed.Rotation() + newRotation;
		frc::Pose3d newPose { seed.Translation(), newRotation };
		poses.push_back(newPose);

		frc::Pose3d nextPose = newPose
				+ frc::Transform3d { obj.second->getObject2dRange(), 0_m, 0_m,
						{ } };
		std::vector < frc::Pose3d > morePoses = obj.second->generate3dMechanism(
				nextPose);
		poses.insert(poses.end(), morePoses.begin(), morePoses.end());
	}
	return poses;
}
