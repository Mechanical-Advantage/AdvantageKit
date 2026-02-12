#pragma once
#include "akit/LogTable.h"

namespace akit {

class Logger {
    private:
    static constexpr int RECEIVER_QUEUE_CAPACITY = 500;

    static bool running;
    static long cycleCount;
    static LogTable entry;
    static LogTable outputTable;
    static std::unordered_map<std::string, std::string> metadata;
};

}