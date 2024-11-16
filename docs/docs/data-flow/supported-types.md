---
sidebar_position: 1
---

# Supported Types

Data is stored using string keys where slashes are used to denote subtables (similar to NetworkTables). Like NetworkTables, **all logged values are persistent (they will continue to appear on subsequent cycles until updated**).

### Simple

The following simple data types are currently supported:

- Single values: `boolean, int, long, float, double, String`
- Arrays: `boolean[], int[], long[], float[], double[], String[], byte[]`
- 2D Arrays: `boolean[][], int[][], long[][], float[][], double[][], String[][], byte[][]`

### Structured

Many WPILib classes can be serialized to binary data using [structs](https://github.com/wpilibsuite/allwpilib/blob/main/wpiutil/doc/struct.adoc) or [protobufs](https://protobuf.dev). Supported classes include `Translation2d`, `Pose3d`, and `SwerveModuleState` with more coming soon. These classes can be logged as single values, arrays, or 2D arrays just like any simple type, and used as input or output fields.

AdvantageKit also supports logging the state of a 2D mechanism object as an output. For details, see [here](/recording-outputs/mechanism2d).

### Units

WPILib includes a [units library](https://docs.wpilib.org/en/latest/docs/software/basic-programming/java-units.html) that can be used to simplify unit conversions. `Measure` objects can be logged and replayed by AdvantageKit. These values will be stored in the log as doubles using the [base unit](https://github.com/wpilibsuite/allwpilib/blob/main/wpiunits/src/main/java/edu/wpi/first/units/BaseUnits.java) for the measurement type (e.g. distances will always be logged in meters).

### Enums

[Enum](https://www.w3schools.com/java/java_enums.asp) values can be logged and replayed by AdvantageKit. These values will be stored in the log as string values (using the [`name()`](https://docs.oracle.com/javase/8/docs/api/java/lang/Enum.html#name--) method).
