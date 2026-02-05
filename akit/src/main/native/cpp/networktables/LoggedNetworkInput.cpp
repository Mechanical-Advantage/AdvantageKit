#include "akit/networktables/LoggedNetworkInput.h"

using namespace akit::nt;

std::string LoggedNetworkInput::removeSlash(std::string key) {
  if (key.starts_with("/")) return key.substr(1);
  return key;
}