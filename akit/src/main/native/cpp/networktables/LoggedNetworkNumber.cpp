#include "akit/networktables/LoggedNetworkNumber.h"

using namespace akit::nt;

LoggedNetworkNumber::LoggedNetworkNumber(std::string key) : key{key}, entry{::nt::NetworkTableInstance::GetDefault().GetDoubleTopic(key).GetEntry(0)}, value{defaultValue} {}

LoggedNetworkNumber::LoggedNetworkNumber(std::string key, double defaultValue) : LoggedNetworkNumber{key} {
    setDefault(defaultValue);
    value = defaultValue;
}

void LoggedNetworkNumber::setDefault(double defaultValue) {
    this->defaultValue = defaultValue;
    entry.Set(entry.Get(defaultValue));
}

void LoggedNetworkNumber::set(double value) {
    entry.Set(value);
}

double LoggedNetworkNumber::get() {
    return entry.Get();
}

void LoggedNetworkNumber::toLog(LogTable&& table) {
    table.put(removeSlash(key), value);
}

void LoggedNetworkNumber::fromLog(LogTable&& table) {
    value = table.get(removeSlash(key), defaultValue);
}

void LoggedNetworkNumber::periodic() {}