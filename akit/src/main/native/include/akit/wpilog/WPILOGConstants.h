#pragma once
#include <string_view>

namespace akit {

namespace wpilog {

class WPILOGConstants {
    public:
    static constexpr std::string_view EXTRA_HEADER = "AdvantageKit";
    static constexpr std::string_view EXTRA_METADATA = "{\"source\":\"AdvantageKit\"}";
    static constexpr std::string_view ENTRY_METADATA_UNITS = "{\"source\":\"AdvantageKit\",\"unit\":\"$UNITSTR\"}";
};

}

}