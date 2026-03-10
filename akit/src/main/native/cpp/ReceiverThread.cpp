// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include "akit/ReceiverThread.h"

using namespace akit;

ReceiverThread::ReceiverThread(
		moodycamel::BlockingConcurrentQueue<LogTable> &queue) : queue { queue } {
}

void ReceiverThread::Start() {
	thread = std::make_unique < std::thread > (&ReceiverThread::Run, this);
}

void ReceiverThread::End() {
	running = false;
	thread->join();
	thread.release();
}

void ReceiverThread::AddDataReceiver(
		std::unique_ptr<LogDataReceiver> receiver) {
	dataReceivers.emplace_back(std::move(receiver));
}

void ReceiverThread::Run() {
	for (auto &receiver : dataReceivers)
		receiver->Start();

	while (running) {
		std::optional < LogTable > entry;
		queue.wait_dequeue(entry);

		for (auto &receiver : dataReceivers)
			receiver->PutTable(*entry);
	}
	std::optional < LogTable > entry;
	while (queue.try_dequeue(entry)) {
		for (auto &receiver : dataReceivers)
			receiver->PutTable(*entry);
	}
}
