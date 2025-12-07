---
sidebar_position: 2
---

# What's New in 2026?

<!-- <h2 style={{textAlign: "center"}}>Lorem Ipsum</h2> -->

## 🧮 Unit Logging

All logging interfaces now support specifying unit metadata compatible with [unit-aware graphing](https://docs.advantagescope.org/tab-reference/line-graph/units) in AdvantageScope. Several examples of unit metadata logging are shown in the code block below. For more details, check the documentation [here](/data-flow/supported-types#units).

```java
Logger.recordOutput("MyAngle", 3.14, "radians");
Logger.recordOutput("MyDistance", Meters.of(4.2));

@AutoLog
public class Inputs {
    public Distance current = Amps.of(63.28);
}

public class Outputs {
    @AutoLogOutput(unit = "inches")
    private double setpoint = 44600.0;

    @AutoLogOutput
    private Distance volts = Volts.of(12.6);
}
```

## 🛜 NetworkTables Client Logging

## 💬 Improved Console Logging

Console logging of exceptions
Replay watch console output

## 📦 Log Mechanisms as 3D Components

TODO

## 🎨 Color Logging

## 🦤 New TalonFX(S) Swerve Template

## 📒 Online API Documentation

API documentation
Reorganization of sidebar
Add individual case studies

## 🦉 All-New Log Replay Comparison
