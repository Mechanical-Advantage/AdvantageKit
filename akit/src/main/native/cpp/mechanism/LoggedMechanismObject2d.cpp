// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include "akit/mechanism/LoggedMechanismObject2d.h"

using namespace akit::mech;

LoggedMechanismObject2d* LoggedMechanismObject2d::Append(
		std::unique_ptr<LoggedMechanismObject2d> object) {
	std::lock_guard lock { mutex };
	if (objects.contains(object->GetName()))
		throw std::runtime_error { "Mechanism object names must be unique!" };

	auto obj = object.get();
	objects[object->GetName()] = std::move(object);

	if (table)
		obj->Update(table->GetSubTable(obj->GetName()));
	return obj;
}

void LoggedMechanismObject2d::Update(std::shared_ptr<nt::NetworkTable> table) {
	std::lock_guard lock { mutex };
	this->table = table;
	UpdateEntries(this->table);
	for (auto &obj : objects)
		obj.second->Update(table->GetSubTable(obj.second->name));
}

void LoggedMechanismObject2d::LogOutput(LogTable &&table) {
	std::lock_guard lock { mutex };
	for (auto &obj : objects)
		obj.second->LogOutput(table.GetSubtable(obj.second->name));
}

std::vector<frc::Pose3d> LoggedMechanismObject2d::Generate3dMechanism(
		frc::Pose3d seed) {
	std::vector < frc::Pose3d > poses;

	for (auto &obj : objects) {
		frc::Rotation3d newRotation { 0_deg, -obj.second->GetAngle(), 0_deg };
		newRotation = seed.Rotation() + newRotation;
		frc::Pose3d newPose { seed.Translation(), newRotation };
		poses.push_back(newPose);

		frc::Pose3d nextPose = newPose
				+ frc::Transform3d { obj.second->GetObject2dRange(), 0_m, 0_m,
						{ } };
		std::vector < frc::Pose3d > morePoses = obj.second->Generate3dMechanism(
				nextPose);
		poses.insert(poses.end(), morePoses.begin(), morePoses.end());
	}
	return poses;
}
