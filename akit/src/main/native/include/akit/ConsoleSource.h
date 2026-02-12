#pragma once
#include <string>

namespace akit {

class ConsoleSource {
    public:
    virtual std::string getNewData() = 0;
};

class SimulatorConsoleSource : public ConsoleSource {
    public:
    SimulatorConsoleSource();
    ~SimulatorConsoleSource();

    std::string getNewData() override;

    private:
    std::streambuf* originalStdout;
    std::streambuf* originalStderr;
};

}