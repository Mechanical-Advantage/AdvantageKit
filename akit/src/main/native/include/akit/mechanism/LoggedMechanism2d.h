// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#pragma once
#include <networktables/NTSendable.h>
#include <networktables/DoubleArrayTopic.h>
#include <networktables/StringTopic.h>
#include <frc/util/Color8Bit.h>
#include "akit/mechanism/LoggedMechanismRoot2d.h"

namespace akit {

namespace mech {

class LoggedMechanism2d: public nt::NTSendable {
public:
	LoggedMechanism2d(units::meter_t width, units::meter_t height) : LoggedMechanism2d {
			width, height, frc::Color8Bit { 0, 0, 32 } } {
	}

	LoggedMechanism2d(units::meter_t width, units::meter_t height,
			frc::Color8Bit backgroundColor) : dims { width.value(),
			height.value() } {
		setBackgroundColor(backgroundColor);
	}

	LoggedMechanismRoot2d& getRoot(std::string name, units::meter_t x,
			units::meter_t y);

	void setBackgroundColor(frc::Color8Bit color);

	void logOutput(LogTable &&table);

	std::vector<frc::Pose3d> generate3dMechanism();

	void InitSendable(nt::NTSendableBuilder &builder) override;

private:
	std::shared_ptr<nt::NetworkTable> table;
	std::unordered_map<std::string, LoggedMechanismRoot2d> roots;
	wpi::array<double, 2> dims;
	std::string color;
	nt::DoubleArrayPublisher dimsPub;
	nt::StringPublisher colorPub;
};

}

}
