#include "akit/networktables/LoggedNetworkBoolean.h"

using namespace akit::nt;

LoggedNetworkBoolean::LoggedNetworkBoolean(std::string key) : key{key}, entry{::nt::NetworkTableInstance::GetDefault().GetBooleanTopic(key).GetEntry(false)}, value{defaultValue} {
}

LoggedNetworkBoolean::LoggedNetworkBoolean(std::string key, bool defaultValue) : LoggedNetworkBoolean{key} {
  setDefault(defaultValue);
  value = defaultValue;
}

void LoggedNetworkBoolean::setDefault(bool defaultValue) {
  this->defaultValue = defaultValue;
  entry.Set(entry.Get(defaultValue));
}

void LoggedNetworkBoolean::set(bool value) {
  entry.Set(value);
}

bool LoggedNetworkBoolean::get() {
  return value;
}

void LoggedNetworkBoolean::toLog(LogTable&& table) {
  table.put(removeSlash(key), value);
}

void LoggedNetworkBoolean::fromLog(LogTable&& table) {
  value = table.get(removeSlash(key), defaultValue);
}

void LoggedNetworkBoolean::periodic() {}