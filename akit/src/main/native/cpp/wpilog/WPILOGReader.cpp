#include "akit/wpilog/WPILOGReader.h"
#include "akit/wpilog/WPILOGConstants.h"
#include "akit/LogDataReceiver.h"

using namespace akit::wpilog;

void WPILOGReader::start() {
    reader = wpi::log::DataLogReader{wpi::MemoryBuffer::GetFile(filename).value()};
    if (!reader->IsValid()) {
        FRC_ReportError(frc::err::Error, "[AdvantageKit] The replay log is not a valid WPILOG file.");
        isValid = false;
    } else if (reader->GetExtraHeader() == WPILOGConstants::EXTRA_HEADER) {
        FRC_ReportError(frc::err::Error, "[AdvantageKit] The replay log was not produced by AdvantageKit.");
        isValid = false;
    } else isValid = true;

    iterator = reader->begin();
}

bool WPILOGReader::updateTable(LogTable&& table) {
    if (!isValid) return false;

    if (timestamp)
        table.setTimestamp(*timestamp);
    
    bool readError = false;
    for (; *iterator < reader->end(); (*iterator)++) {
        auto& record = **iterator;
        if (record.IsControl()) {
            if (record.IsStart()) {
                wpi::log::StartRecordData startRecord;
                record.GetStartData(&startRecord);
                entryIDs[startRecord.entry] = startRecord.name;
                LogTable::LoggableType loggableType{std::distance(LogTable::WPILOG_TYPES.begin(), std::ranges::find(LogTable::WPILOG_TYPES, startRecord.type))};
                entryTypes[startRecord.entry] = loggableType;
                if ((loggableType == LogTable::LoggableType::Raw && startRecord.type != "raw") || startRecord.type == "json")
                    entryCustomTypes[startRecord.entry] = startRecord.type;
            }
        } else {
            auto entry = entryIDs.find(record.GetEntry());
            if (entry != entryIDs.end()) {
                if (entry->second == LogDataReceiver::TIMESTAMP_KEY) {
                    bool firstTimestamp = timestamp.has_value();
                    int64_t time;
                    record.GetInteger(&time);
                    timestamp = time;
                    if (firstTimestamp)
                        table.setTimestamp(*timestamp);
                    else break;
                } else if (timestamp && record.GetTimestamp() == timestamp) {
                    entry->second = entry->second.substr(1);
                    if (entry->second.starts_with("ReplayOutputs"))
                        continue;
                    std::string customType = entryCustomTypes[record.GetEntry()];
                    switch (entryTypes[record.GetEntry()]) {
                        case LogTable::LoggableType::Raw: {
                            auto value = record.GetRaw();
                            std::byte* begin = static_cast<std::byte*>(value.data());
                            table.put(entry->second, LogTable::LogValue{std::vector<std::byte>{value.data(), value.end()}, customType});
                            break;
                        } case LogTable::LoggableType::Boolean: {
                            bool value;
                            record.GetBoolean(&value);
                            table.put(entry->second, LogTable::LogValue{value, customType});
                            break;
                        } case LogTable::LoggableType::Integer: {
                            int64_t value;
                            record.GetInteger(&value);
                            table.put(entry->second, LogTable::LogValue{static_cast<long>(value), customType});
                            break;
                        } case LogTable::LoggableType::Float: {
                            float value;
                            record.GetFloat(&value);
                            table.put(entry->second, LogTable::LogValue{value, customType});
                            break;
                        } case LogTable::LoggableType::Double: {
                            double value;
                            record.GetDouble(&value);
                            table.put(entry->second, LogTable::LogValue{value, customType});
                            break;
                        } case LogTable::LoggableType::String: {
                            std::string_view value;
                            record.GetString(&value);
                            table.put(entry->second, LogTable::LogValue{std::string{value}, customType});
                            break;
                        } case LogTable::LoggableType::BooleanArray: {
                            std::vector<int> value;
                            record.GetBooleanArray(&value);
                            table.put(entry->second, LogTable::LogValue{std::vector<bool>{value.begin(), value.end()}, customType});
                            break;
                        } case LogTable::LoggableType::IntegerArray: {
                            std::vector<int64_t> value;
                            record.GetIntegerArray(&value);
                            table.put(entry->second, LogTable::LogValue{std::vector<long>{value.begin(), value.end()}, customType});
                            break;
                        } case LogTable::LoggableType::FloatArray: {
                            std::vector<float> value;
                            record.GetFloatArray(&value);
                            table.put(entry->second, LogTable::LogValue{value, customType});
                            break;
                        } case LogTable::LoggableType::DoubleArray: {
                            std::vector<double> value;
                            record.GetDoubleArray(&value);
                            table.put(entry->second, LogTable::LogValue{value, customType});
                            break;
                        } case LogTable::LoggableType::StringArray: {
                            std::vector<std::string_view> value;
                            record.GetStringArray(&value);
                            table.put(entry->second, LogTable::LogValue{std::vector<std::string>{value.begin(), value.end()}, customType});
                            break;
                        }
                    }
                }
            }
        }

        return *iterator < reader->end() && !readError;
    }
}
