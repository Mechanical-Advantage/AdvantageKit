// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include "akit/ReceiverThread.h"

using namespace akit;

ReceiverThread::ReceiverThread(std::queue<LogTable> queue) : queue { queue } {
	thread.detach();
}

void ReceiverThread::addDataReceiver(
		std::unique_ptr<LogDataReceiver> receiver) {
	dataReceivers.emplace_back(receiver);
}

void ReceiverThread::run() {
	for (auto &receiver : dataReceivers)
		receiver->start();

	while (true) {
		LogTable entry = queue.front();
		queue.pop();

		for (auto &receiver : dataReceivers)
			receiver->putTable(entry);
	}
}
