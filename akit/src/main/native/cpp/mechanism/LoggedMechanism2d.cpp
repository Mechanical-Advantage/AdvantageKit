// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include <networktables/NTSendableBuilder.h>
#include "akit/mechanism/LoggedMechanism2d.h"

using namespace akit::mech;

LoggedMechanismRoot2d& LoggedMechanism2d::getRoot(std::string name,
		units::meter_t x, units::meter_t y) {
	auto existing = roots.find(name);
	if (existing != roots.end())
		return existing->second;

	roots.emplace(name, LoggedMechanismRoot2d { name, x, y });
	if (table)
		roots.at(name).update(table->GetSubTable(name));
	return roots.at(name);
}

void LoggedMechanism2d::setBackgroundColor(frc::Color8Bit color) {
	std::lock_guard lock { mutex };
	this->color = color.HexString();
	colorPub.Set(this->color);
}

void LoggedMechanism2d::InitSendable(nt::NTSendableBuilder &builder) {
	builder.SetSmartDashboardType("Mechanism2d");
	{
		std::lock_guard lock { mutex };
		table = builder.GetTable();
		dimsPub = table->GetDoubleArrayTopic("dims").Publish();
		dimsPub.Set(dims);
		colorPub = table->GetStringTopic("backgroundColor").Publish();
		colorPub.Set(color);
		for (auto &root : roots)
			root.second.update(table->GetSubTable(root.first));
	}
}

void LoggedMechanism2d::logOutput(LogTable &&table) {
	std::lock_guard lock { mutex };
	table.put(".type", "Mechanism2d");
	table.put(".controllable", false);
	table.put("dims", std::vector<double> { dims.begin(), dims.end() });
	table.put("backgroundColor", color);
	for (auto &root : roots)
		root.second.logOutput(table.getSubtable(root.first));
}
