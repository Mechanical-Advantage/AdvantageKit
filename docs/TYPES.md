# Supported Data Types & Conversions

| NT3            | <-->                       | Core (WPILOG/NT4)      | <-->                                         | RLOG                        |
| -------------- | -------------------------- | ---------------------- | -------------------------------------------- | --------------------------- |
| `Raw`          | <-->                       | `Raw`                  | <-->                                         | `0x0A ByteArray`            |
|                |                            |                        | <-- Store as Raw                             | `0x09 Byte`                 |
| `Boolean`      | <-->                       | `Boolean`              | <-->                                         | `0x01 Boolean`              |
|                | <-- Convert to Double      | `Integer (int64)`      | <-- Convert (lossless) / Convert (lossy) --> | `0x03 Integer (int32)`      |
|                | <-- Convert to Double      | `Float`                | Convert to Double -->                        |                             |
| `Double`       | <-->                       | `Double`               | <-->                                         | `0x05 Double`               |
| `String`       | <-->                       | `String`               | <-->                                         | `0x07 String`               |
| `BooleanArray` | <-->                       | `BooleanArray`         | <-->                                         | `0x02 BooleanArray`         |
|                | <-- Convert to DoubleArray | `IntegerArray (int64)` | <-- Convert (lossless) / Convert (lossy) --> | `0x04 IntegerArray (int32)` |
|                | <-- Convert to DoubleArray | `FloatArray`           | Convert to DoubleArray -->                   |                             |
| `DoubleArray`  | <-->                       | `DoubleArray`          | <-->                                         | `0x06 DoubleArray`          |
| `StringArray`  | <-->                       | `StringArray`          | <-->                                         | `0x08 StringArray`          |
| `Unassigned`   | Ignored -->                |                        |                                              |                             |
| `Rpc`          | Ignored -->                |                        |                                              |                             |
