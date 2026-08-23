// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include "akit/ReceiverThread.h"

using namespace akit;

ReceiverThread::ReceiverThread(
		moodycamel::BlockingReaderWriterCircularBuffer<LogTable> &queue) : queue { queue } {
}

void ReceiverThread::Start() {
	thread = std::make_unique < std::jthread > ([this](std::stop_token stopToken) { Run(stopToken); });
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

void ReceiverThread::Run(std::stop_token stopToken) {
	for (auto &receiver : dataReceivers)
		receiver->Start();

	std::optional<LogTable> entry;
	while (!stopToken.stop_requested()) {
		if (queue.wait_dequeue_timed(entry, std::chrono::milliseconds{100}))
			for (auto &receiver : dataReceivers)
				receiver->PutTable(*entry);
	}

	while (queue.try_dequeue(entry)) {
		for (auto &receiver : dataReceivers)
			receiver->PutTable(*entry);
	}
}
