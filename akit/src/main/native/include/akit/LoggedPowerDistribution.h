#pragma once
#include "akit/LogTable.h"
#include "akit/conduit/ConduitApi.h"

namespace akit {

class LoggedPowerDistribution {
    public:
    static LoggedPowerDistribution& getInstance() {
        if (!instance)
            instance = std::make_unique<LoggedPowerDistribution>();
        return instance;
    }

    static LoggedPowerDistribution& getInstance(int moduleID, HAL_PowerDistributionType moduleType) {
        if (!instance) instance = std::make_unique<LoggedPowerDistribution>(moduleID, moduleType);
        return instance;
    }

    void saveToLog(LogTable table) {
        conduit::ConduitApi& inst = conduit::ConduitApi::getInstance();
        table.put("Temperature", inst.getPDPTemperature());
        table.put("Voltage", inst.getPDPVoltage());
        std::array<double, 24> currents;
        for (int i = 0; i < currents.size(); i++)
            currents[i] = inst.getPDPChannelCurrent(i);
        table.put("ChannelCurrent", currents);
        table.put("TotalCurrent", inst.getPDPTotalCurrent());
        table.put("TotalPower", inst.getPDPTotalPower());
        table.put("TotalEnergy", inst.getPDPTotalEnergy());

        table.put("ChannelCount", inst.getPDPChannelCount());
        table.put("Faults", inst.getPDPFaults());
        table.put("StickyFaults", inst.getPDPStickyFaults());
    }

    private:
    LoggedPowerDistribution() : LoggedPowerDistribution{HAL_DEFAULT_POWER_DISTRIBUTION_MODULE, HAL_PowerDistributionType_kAutomatic} {}

    LoggedPowerDistribution(int moduleID, HAL_PowerDistributionType moduleType) : moduleID{moduleID}, moduleType{moduleType} {
        conduit::ConduitApi::getInstance().configurePowerDistribution(moduleID, moduleType);
    }

    static std::unique_ptr<LoggedPowerDistribution> instance;
    int moduleID;
    HAL_PowerDistributionType moduleType;
}

}