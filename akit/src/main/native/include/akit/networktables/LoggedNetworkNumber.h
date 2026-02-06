#pragma once
#include <networktables/DoubleTopic.h>
#include <networktables/NetworkTableInstance.h>
#include "akit/networktables/LoggedNetworkInput.h"
#include "akit/inputs/LoggableInputs.h"
#include "akit/LogTable.h"

namespace akit {

namespace nt {

class LoggedNetworkNumber : public LoggedNetworkInput, public inputs::LoggableInputs {
    public:
    LoggedNetworkNumber(std::string key) : key{key}, entry{::nt::NetworkTableInstance::GetDefault().GetDoubleTopic(key).GetEntry(0)}, value{defaultValue} {
        // Logger.registerDashboardInput(this);
    }

    LoggedNetworkNumber(std::string key, double defaultValue) : LoggedNetworkNumber{key} {
        setDefault(defaultValue);
        value = defaultValue;
    }

    void setDefault(double defaultValue) {
        this->defaultValue = defaultValue;
        entry.Set(entry.Get(defaultValue));
    }

    void set(double value) {
        entry.Set(value);
    }

    double get() {
        return value;
    }

    void toLog(LogTable table) override {
        table.put(removeSlash(key), value);
    }

    void fromLog(LogTable table) override {
        value = table.get(removeSlash(key), defaultValue);
    }

    void periodic() {
    // if (!Logger.hasReplaySource()) {
    //   value = entry.get(defaultValue);
    // }
    // Logger.processInputs(prefix, inputs);
  }

    private:
    std::string key;
    ::nt::DoubleEntry entry;
    double defaultValue;
    double value;
};

}

}