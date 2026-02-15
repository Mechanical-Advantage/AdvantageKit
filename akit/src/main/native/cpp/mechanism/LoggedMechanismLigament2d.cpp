// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include "akit/mechanism/LoggedMechanismLigament2d.h"

using namespace akit::mech;

LoggedMechanismLigament2d::LoggedMechanismLigament2d(std::string name,
		units::meter_t length, units::degree_t angle, double lineWidth,
		frc::Color8Bit color) : LoggedMechanismObject2d { name } {
	SetColor(color);
	SetLength(length);
	SetAngle(angle);
	SetLineWeight(lineWidth);
}

void LoggedMechanismLigament2d::SetAngle(units::degree_t angle) {
	std::lock_guard lock { mutex };
	this->angle = angle;
	angleEntry.Set(angle.value());
}

units::degree_t LoggedMechanismLigament2d::GetAngle() {
	std::lock_guard lock { mutex };
	if (angleEntry)
		angle = units::degree_t { angleEntry.Get() };
	return angle;
}

void LoggedMechanismLigament2d::SetLength(units::meter_t length) {
	std::lock_guard lock { mutex };
	this->length = length;
	lengthEntry.Set(length.value());
}

units::meter_t LoggedMechanismLigament2d::GetLength() {
	std::lock_guard lock { mutex };
	if (lengthEntry)
		length = units::meter_t { lengthEntry.Get() };
	return length;
}

void LoggedMechanismLigament2d::SetColor(frc::Color8Bit color) {
	std::lock_guard lock { mutex };
	this->color = color.HexString();
	colorEntry.Set(this->color);
}

frc::Color8Bit LoggedMechanismLigament2d::GetColor() {
	std::lock_guard lock { mutex };
	if (colorEntry)
		color = colorEntry.Get();
	return frc::Color8Bit::FromHexString(color);
}

void LoggedMechanismLigament2d::SetLineWeight(double weight) {
	std::lock_guard lock { mutex };
	this->weight = weight;
	weightEntry.Set(weight);
}

double LoggedMechanismLigament2d::GetLineWeight() {
	std::lock_guard lock { mutex };
	if (weightEntry)
		weight = weightEntry.Get();
	return weight;
}

void LoggedMechanismLigament2d::UpdateEntries(
		std::shared_ptr<nt::NetworkTable> table) {
	typePub = table->GetStringTopic(".type").Publish();
	typePub.Set("line");

	angleEntry = table->GetDoubleTopic("angle").GetEntry(0);
	angleEntry.Set(angle.value());

	lengthEntry = table->GetDoubleTopic("length").GetEntry(0);
	lengthEntry.Set(length.value());

	colorEntry = table->GetStringTopic("color").GetEntry("");
	colorEntry.Set(color);

	weightEntry = table->GetDoubleTopic("weight").GetEntry(0);
	weightEntry.Set(weight);
}

void LoggedMechanismLigament2d::LogOutput(LogTable &&table) {
	std::lock_guard lock { mutex };
	table.Put(".type", "line");
	table.Put("angle", angle);
	table.Put("length", length);
	table.Put("color", color);
	table.Put("weight", weight);
	LoggedMechanismObject2d::LogOutput(std::move(table));
}
