// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

#include <queue>
#include <thread>
#include <blockingconcurrentqueue.h>
#include "akit/LogTable.h"
#include "akit/LogDataReceiver.h"

namespace akit {

class ReceiverThread {
public:
	void addDataReceiver(std::unique_ptr<LogDataReceiver> receiver);
	ReceiverThread(moodycamel::BlockingConcurrentQueue<LogTable> &queue);

private:
	void run();

	std::thread thread { &ReceiverThread::run, this };
	moodycamel::BlockingConcurrentQueue<LogTable> &queue;
	std::vector<std::unique_ptr<LogDataReceiver>> dataReceivers;
};

}
