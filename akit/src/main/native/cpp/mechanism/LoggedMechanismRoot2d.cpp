// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include "akit/mechanism/LoggedMechanismRoot2d.h"

using namespace akit::mech;

LoggedMechanismObject2d* LoggedMechanismRoot2d::Append(
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

void LoggedMechanismRoot2d::SetPosition(units::meter_t x, units::meter_t y) {
	std::lock_guard lock { mutex };
	this->x = x;
	this->y = y;
	Flush();
}

void LoggedMechanismRoot2d::Update(std::shared_ptr<nt::NetworkTable> table) {
	std::lock_guard lock { mutex };
	this->table = table;

	xPub = this->table->GetDoubleTopic("x").Publish();
	yPub = this->table->GetDoubleTopic("y").Publish();
	Flush();
	for (auto &obj : objects)
		obj.second->Update(this->table->GetSubTable(obj.second->GetName()));
}

void LoggedMechanismRoot2d::Flush() {
	xPub.Set(x.value());
	yPub.Set(y.value());
}

void LoggedMechanismRoot2d::LogOutput(LogTable &&table) {
	std::lock_guard lock { mutex };
	table.Put("x", x);
	table.Put("y", y);
	for (auto &obj : objects)
		obj.second->LogOutput(table.GetSubtable(obj.second->GetName()));
}

std::vector<frc::Pose3d> LoggedMechanismRoot2d::Generate3dMechanism() {
	std::vector < frc::Pose3d > poses;

	frc::Pose3d initialPose { x, 0_m, y, { } };
	for (auto &obj : objects) {
		frc::Rotation3d newRotation { 0_deg, -obj.second->GetAngle(), 0_deg };

		frc::Pose3d newPose { initialPose.Translation(), newRotation };
		poses.push_back(newPose);

		frc::Pose3d nextPose = newPose
				+ frc::Transform3d { obj.second->GetObject2dRange(), 0_m, 0_m,
						{ } };
		std::vector < frc::Pose3d > morePoses = obj.second->Generate3dMechanism(
				nextPose);
		poses.insert(poses.begin(), morePoses.begin(), morePoses.end());
	}
	return poses;
}
