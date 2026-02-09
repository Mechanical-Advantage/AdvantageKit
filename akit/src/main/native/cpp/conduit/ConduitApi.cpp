// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include "akit/conduit/ConduitApi.h"

using namespace akit::conduit;

ConduitApi& ConduitApi::getInstance() {
	static ConduitApi instance;
	return instance;
}

org::littletonrobotics::conduit::schema::CoreInputs& ConduitApi::getCoreInputs() {
	wpilibio::start();
	if (wpilibio::shared_buf == 0)
		wpilibio::make_buffer();
	return *static_cast<org::littletonrobotics::conduit::schema::CoreInputs*>(wpilibio::shared_buf);
}
