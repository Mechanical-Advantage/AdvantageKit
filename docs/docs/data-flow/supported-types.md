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

:::danger
Protobuf logging can take an extended period (>100ms) the first time that a value with any given type is logged. Subsequent logging calls using an object of the same type will be significantly faster. **Protobuf values should always be logged for the first time when the robot is disabled.**

_This issue is not applicable to struct logging, which is the default for all data types._
:::

### Records

Custom [record](https://www.baeldung.com/java-record-keyword) classes can be logged as structs, including support for single values, arrays, and 2D arrays as inputs or outputs. This enables efficient logging of custom complex data types, such as pose observations (check the [vision template](/getting-started/template-projects/vision-template) for examples).

Note that record fields must use only the following struct-compatible types. Array types are not supported for record fields. We recommend logging using multiple top-level record arrays as needed.

- Primitives: `boolean`, `short`, `int`, `long`, `float`, `double`
- Enum values
- Struct-compatible types (`Pose2d`, `SwerveModuleState`, etc.)
- Record values (i.e. nested records)

:::tip
Logging multiple record types of the same name can cause conflicts. All record classes should be uniquely named.
:::

:::danger
Record logging can take an extended period (>100ms) the first time that a value with any given type is logged. Subsequent logging calls using an object of the same type will be significantly faster. **Record values should always be logged for the first time when the robot is disabled.**
:::

### Units

WPILib includes a [units library](https://docs.wpilib.org/en/latest/docs/software/basic-programming/java-units.html) that can be used to simplify unit conversions. `Measure` objects can be logged and replayed by AdvantageKit. These values will be stored in the log as doubles using the [base unit](https://github.com/wpilibsuite/allwpilib/blob/main/wpiunits/src/main/java/edu/wpi/first/units/BaseUnits.java) for the measurement type (e.g. distances will always be logged in meters).

### Enums

[Enum](https://www.w3schools.com/java/java_enums.asp) values can be logged and replayed by AdvantageKit. These values will be stored in the log as string values (using the [`name()`](https://docs.oracle.com/javase/8/docs/api/java/lang/Enum.html#name--) method).

### Suppliers (Output Only)

Primitive suppliers (`BooleanSupplier`, `IntSupplier`, `LongSupplier`, and `DoubleSupplier`) can be used in place of their single values for output logging, including annotation logging with `@AutoLogOutput`. One application of this feature is logging [`Trigger`](https://github.wpilib.org/allwpilib/docs/release/java/edu/wpi/first/wpilibj2/command/button/Trigger.html) values, which extend from `BooleanSupplier`.

### Mechanisms (Output Only)

AdvantageKit can log 2D mechanism objects as outputs, which can be viewed using AdvantageScope. If not using `@AutoLogOutput`, note that the logging call only records the current state of the `Mechanism2d` and so it must be called periodically.

:::warning
Mechanism objects must use the **`LoggedMechanism2d`** class to be compatible with AdvantageKit. This class is otherwise equivalent to the standard `Mechanism2d` class. Equivalent `LoggedMechanismRoot2d`, `LoggedMechanismObject2d`, and `LoggedMechanismLigament2d` classes are also provided.
:::

```java
public class Example {
    @AutoLogOutput // Auto logged as "Example/Mechanism"
    private LoggedMechanism2d mechanism = new LoggedMechanism2d(3, 3);

    public void periodic() {
        // Alternative approach if not using @AutoLogOutput
        // (Must be called periodically)
        Logger.recordOutput("Example/Mechanism", mechanism);
    }
}
```
