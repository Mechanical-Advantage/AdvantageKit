// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once
#include <units/mass.h>
#include <networktables/StringTopic.h>
#include <networktables/DoubleTopic.h>
#include <frc/util/Color8Bit.h>
#include "akit/mechanism/LoggedMechanismObject2d.h"

namespace akit {

namespace mech {

class LoggedMechanismLigament2d: public LoggedMechanismObject2d {
public:
	LoggedMechanismLigament2d(std::string name, units::meter_t length,
			units::degree_t angle, double lineWidth, frc::Color8Bit color);

	LoggedMechanismLigament2d(std::string name, units::meter_t length,
			units::degree_t angle) : LoggedMechanismLigament2d { name, length,
			angle, 10, frc::Color8Bit { 235, 137, 52 } } {
	}

	void setAngle(units::degree_t angle);

	void setAngle(frc::Rotation2d angle) {
		setAngle(angle.Degrees());
	}

	units::degree_t getAngle() override;

	void setLength(units::meter_t);

	units::meter_t getLength();

	void setColor(frc::Color8Bit color);

	frc::Color8Bit getColor();

	void setLineWeight(double weight);

	double getLineWeight();

protected:
	void updateEntries(std::shared_ptr<nt::NetworkTable> table) override;

private:
	void logOutput(LogTable &&table) override;

	units::meter_t getObject2dRange() override {
		return getLength();
	}

	nt::StringPublisher typePub;
	units::degree_t angle;
	nt::DoubleEntry angleEntry;
	std::string color;
	nt::StringEntry colorEntry;
	units::meter_t length;
	nt::DoubleEntry lengthEntry;
	double weight;
	nt::DoubleEntry weightEntry;
	std::mutex mutex;
};

}

}
