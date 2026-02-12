// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include "akit/mechanism/LoggedMechanismRoot2d.h"

using namespace akit::mech;

LoggedMechanismObject2d* LoggedMechanismRoot2d::append(
		std::unique_ptr<LoggedMechanismObject2d> object) {
	std::lock_guard lock { mutex };
	if (objects.contains(object->getName()))
		throw std::runtime_error { "Mechanism object names must be unique!" };

	auto obj = object.get();
	objects[object->getName()] = std::move(object);

	if (table)
		obj->update(table->GetSubTable(obj->getName()));
	return obj;
}

void LoggedMechanismRoot2d::setPosition(units::meter_t x, units::meter_t y) {
	std::lock_guard lock { mutex };
	this->x = x;
	this->y = y;
	flush();
}

void LoggedMechanismRoot2d::update(std::shared_ptr<nt::NetworkTable> table) {
	std::lock_guard lock { mutex };
	this->table = table;

	xPub = this->table->GetDoubleTopic("x").Publish();
	yPub = this->table->GetDoubleTopic("y").Publish();
	flush();
	for (auto &obj : objects)
		obj.second->update(this->table->GetSubTable(obj.second->getName()));
}

void LoggedMechanismRoot2d::flush() {
	xPub.Set(x.value());
	yPub.Set(y.value());
}

void LoggedMechanismRoot2d::logOutput(LogTable &&table) {
	std::lock_guard lock { mutex };
	table.put("x", x);
	table.put("y", y);
	for (auto &obj : objects)
		obj.second->logOutput(table.getSubtable(obj.second->getName()));
}

std::vector<frc::Pose3d> LoggedMechanismRoot2d::generate3dMechanism() {
	std::vector < frc::Pose3d > poses;

	frc::Pose3d initialPose { x, 0_m, y, { } };
	for (auto &obj : objects) {
		frc::Rotation3d newRotation { 0_deg, -obj.second->getAngle(), 0_deg };

		frc::Pose3d newPose { initialPose.Translation(), newRotation };
		poses.push_back(newPose);

		frc::Pose3d nextPose = newPose
				+ frc::Transform3d { obj.second->getObject2dRange(), 0_m, 0_m,
						{ } };
		std::vector < frc::Pose3d > morePoses = obj.second->generate3dMechanism(
				nextPose);
		poses.insert(poses.begin(), morePoses.begin(), morePoses.end());
	}
	return poses;
}
