---
sidebar_position: 3
---

# Uninitialized Inputs

## Timestamps & Driver Station

Before calling `Logger.start()`, AdvantageKit's built-in input logging is not active. This means that Driver Station data (e.g. from the `DriverStation` or joystick classes) and timestamp data (e.g. `Timer.getTimestamp()`) are **not deterministic** and **should not be accessed** by the robot code.

:::tip
Wait to create subsystems or button bindings until after `Logger.start()` is called. For most projects, this can be achieved by instantiating `RobotContainer` at the end of the `Robot` constructor.
:::

```java
public class Robot extends LoggedRobot {
    // DANGER: The Intake constructor runs before Logger.start(),
    // so it can access non-deterministic timestamps and DS data.
    private final Intake intake = new Intake();

    // Better: The Flywheel constructor is not called until after
    // Logger.start() has been called in the Robot constructor.
    private final Flywheel flywheel;

    public Robot() {
        // ... (AdvantageKit configuration)
        Logger.start();

        // Since Logger.start() has been called, the Flywheel
        // constructor is free to use timestamps and DS data.
        flywheel = new Flywheel();
    }
}
```

## Subsystems

Typically, inputs from subsystems are only updated during calls to `periodic`. Note that this means updated (non-default) input data is not available in the constructor. The solution is to either wait for the first `periodic` call or call `periodic` from within the constructor.

```java
public class Example extends SubsystemBase {
    private final ExampleIO io;
    private final ExampleIOInputs inputs = new ExampleIOInputs();

    public Example(ExampleIO io) {
        this.io = io;

        // Inputs are not updated yet
        inputs.position;
    }

    @Override
    public void periodic() {
        io.updateInputs(inputs);
        Logger.processInputs("Example", inputs);

        // Inputs are now updated
        inputs.position;
    }
}
```
