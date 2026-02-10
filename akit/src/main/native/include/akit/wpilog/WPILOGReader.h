#pragma once
#include <wpi/DataLogReader.h>
#include "akit/LogReplaySource.h"

namespace akit {

namespace wpilog {

class WPILOGReader : LogReplaySource {
    public:
    WPILOGReader(std::string filename) : filename{filename} {}

    void start() override;
    bool updateTable(LogTable&& table);

    private:
    std::string filename;
    bool isValid;

    std::optional<wpi::log::DataLogReader> reader;
    std::optional<wpi::log::DataLogIterator> iterator;

    std::optional<long> timestamp;
    std::unordered_map<int, std::string> entryIDs;
    std::unordered_map<int, LogTable::LoggableType> entryTypes;
    std::unordered_map<int, std::string> entryCustomTypes;
};

}

}