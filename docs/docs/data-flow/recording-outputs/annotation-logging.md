---
sidebar_position: 1
---

# Annotation Logging

The `@AutoLogOutput` annotation can also be used to automatically log the value of a field or getter method as an output periodically (including private fields and methods). The key will be selected automatically, or it can be overridden using the `key` parameter. All data types are supported, including arrays and structured data types.

```java
public class Example {
    @AutoLogOutput // Logged as "Example/MyPose"
    private Pose2d myPose = new Pose2d();

    @AutoLogOutput(key = "Custom/Speeds")
    public double[] getSpeeds() {...}
}
```

The `key` parameter can reference other fields within the same class using the syntax shown below. This is useful to disambiguate classes with multiple instances, such as swerve modules. The value of the referenced field will not be updated after the first loop cycle. Any data type convertible to a string is supported, including numbers, booleans, and strings.

```java
public class SwerveModule {
    private final int index; // 0, 1, 2, or 3
    private final String descriptor; // "FL", "FR", "BL", "BR"

    @AutoLogOutput(key = "Module{index}/Speed") // e.g. "Module0/Speed"
    public double getSpeed() {...}

    @AutoLogOutput(key = "Odometry/ModulePose{descriptor}") // e.g. "Odometry/ModulePoseFL"
    public Pose2d getPose() {...}
}
```

By default, the parent class where `@AutoLogOutput` is used must be within the same package as `Robot` (or a subpackage). The following method can be called in the constructor of `Robot` to allow additional packages, such as a "lib" package outside of normal robot code:

```java
AutoLogOutputManager.addPackage("frc.lib");
```

The `addObject` method can also be used to manually scan an object for loggable fields. This method should only be called during initialization:

```java
AutoLogOutputManager.addObject(this);
```

:::warning
The parent class where `@AutoLogOutput` is used must also be instantiated within the first loop cycle and be accessible by a recursive search of the fields of `Robot`. This feature is primarily intended to log outputs from subsystems and other similar classes. For classes that do not fit the criteria above, call `Logger.recordOutput` periodically to record outputs.
:::
