#include "akit/ConsoleSource.h"

using namespace akit;

RoboRIOConsoleSource::RoboRIOConsoleSource() {
    thread.detach();
}

std::string RoboRIOConsoleSource::getNewData() {
    lines.clear();
    std::string output;
    while (!queue.empty()) {
        output += queue.front() + "\n";
        queue.pop();
    }
    return output.substr(0, output.size() - 1);
}

void RoboRIOConsoleSource::run() {
    std::vector<char> buffer{10240};
}