# Byte Format for Robot Logs (.rlog)

## Log Revisions

The first byte represents the log format revision. The decoding device should always check whether it supports the specified revision before continuing. Below is a list of possible revisions:

- R1 = Supported by AdvantageKit v0.0.1-v1.8.1 and v2.2.0-v3.1.1. Uses a predefined set of field types.
- R2 = Supported by AdvantageKit v3.2.0 and newer. Uses string names for field types.

All values are stored in big endian order.

## Message Types

The next byte represents the which of 3 message types are being used:

- 0x00 = Timestamp (start of a new cycle)
- 0x01 = Key (defines string value for a key ID)
- 0x02 = Field (value of a single field)

## Timestamp

Following the byte for message type, a timestamp message consists of a single double (8 bytes) representing the timestamp in seconds. This message marks the start of a new cycle and the timestamp should be associated with subsequent fields.

## Key

Each human-readable string key (e.g. "/DriveTrain/LeftPositionRadians") is represented by a key ID to reduce the space required to encode the data from each cycle. Key IDs are shorts (2 bytes) that count up from 0. Each key message contains the following information:

1. Key ID (short, 2 bytes)
2. Number of bytes in string (short, 2 bytes)
3. String key (UTF-8 encoded)
4. Number of bytes in string (short, 2 bytes) - Only if RLOG R2
5. String type (UTF-8 encoded) - Only if RLOG R2

## Field

Field messages represent a change to a single value. The structure of these messages begins with the following information:

1. Key ID (short, 2 bytes)
2. Value type (1 byte) - Only if RLOG R1
2. Value length (short, 2 bytes) - Only if RLOG R2

For RLOG R2, the value can follow any format. By default, use the [WPILOG-specified data types](https://github.com/wpilibsuite/allwpilib/blob/main/wpiutil/doc/datalog.adoc#data-types).

For RLOG R1, the possible value types are listed below along with the format of the value. Null (0x00) does not include more information.

### Boolean (0x01)

3. Field value (0x00 or 0x01, 1 byte)

### BooleanArray (0x02)

3. Length of array (short, 2 bytes)
4. Contents of array (series of 0x00 or 0x01, 1 byte each)

### Integer (0x03)

3. Field value (integer, 4 bytes)

### IntegerArray (0x04)

3. Length of array (short, 2 bytes)
4. Contents of array (integers, 4 bytes each)

### Double (0x05)

3. Field value (double, 8 bytes)

### DoubleArray (0x06)

3. Length of array (short, 2 bytes)
4. Contents of array (doubles, 8 bytes each)

### String (0x07)

3. Number of bytes in string (short, 2 bytes)
4. Field value (UTF-8 encoded)

### StringArray (0x08)

3. Length of array (short, 2 bytes)
4. Contents of array (same format as single string)

### Byte (0x09)

3. Field value (1 byte)

### ByteArray(0x0A)

3. Length of array (short, 2 bytes)
4. Contents of array (series of bytes)

## Networking

When sending log data over a network, the same format is used. Each cycle is preceded by a single integer (4 bytes) containing the number of bytes in the cycle. This allows the client to easily determine when a cycle can be decoded.

For efficiency, the server can encode the same data for every client after the first cycle. When a new client connects, the server should send:

1. The log format revision (described above).
2. Definitions of each pre-existing key ID.
3. Every value from the most recent cycle, regardless of whether any changes occurred.

This information allows the new device to "catch up" and decode the log the same as any older client.
