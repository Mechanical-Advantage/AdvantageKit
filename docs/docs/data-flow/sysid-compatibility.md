---
sidebar_position: 6
---

# SysId Compatibility

WPILib provides tools to perform [system identification](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/system-identification/index.html) on robot mechanisms, enabling [feedforward and feedback](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/controllers/index.html) controller gains to be calculated based on real-world data. Starting in 2024, identification routines are defined in user code as described [here](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/system-identification/creating-routine.html). Data is recorded to a WPILOG file for analysis in the SysId application.

Since AdvantageKit already requires subsystems to log relevant sensor data, setting up identification routines in user code is simplified considerably. This document outlines how the process of collecting SysId data differs when using AdvantageKit for data logging. **Please refer to the [WPILib SysId documentation](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/system-identification/index.html) for more details.**

:::tip
Device logging systems like AdvantageScope's [URCL](https://docs.advantagescope.org/more-features/urcl) (Unofficial REV-Compatible Logger) and CTRE's [signal logger](https://pro.docs.ctr-electronics.com/en/latest/docs/api-reference/api-usage/signal-logging.html) can be used to collect data instead of AdvantageKit. In this case, please follow the instructions in the corresponding documentation.
:::

## Code Setup

Create the `SysIdRoutine` based on the template shown below. Note that the test state is logged as an output through AdvantageKit and the log consumer is set to `null`. This configuration can be performed within the subsystem class.

```java
// Create the SysId routine
var sysIdRoutine = new SysIdRoutine(
  new SysIdRoutine.Config(
    null, null, null, // Use default config
    (state) -> Logger.recordOutput("SysIdTestState", state.toString())
  ),
  new SysIdRoutine.Mechanism(
    (voltage) -> subsystem.runVolts(voltage.in(Volts)),
    null, // No log consumer, since data is recorded by AdvantageKit
    subsystem
  )
);

// The methods below return Command objects
sysIdRoutine.quasistatic(SysIdRoutine.Direction.kForward);
sysIdRoutine.quasistatic(SysIdRoutine.Direction.kReverse);
sysIdRoutine.dynamic(SysIdRoutine.Direction.kForward);
sysIdRoutine.dynamic(SysIdRoutine.Direction.kReverse);
```

Run the SysId routines as normal (described [here](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/system-identification/running-routine.html)), then download the AdvantageKit log file from the robot.

## Loading Data

:::warning
AdvantageKit log files **should NOT be used directly with SysId**. Follow the instructions below to convert it to the correct format.
:::

AdvantageKit synchronizes all log updates to the robot loop cycle to enable replay. However, only _changes_ to each field are recorded directly to the log file; by saving the timestamps of each loop cycle, the full set of timestamps where a field was originally recorded can be recreated during replay. This design was chosen because it significantly reduces file size, but it is not compatible with WPILib's SysId analyzer (where explicit updates are expected for every sample, regardless of whether the value changed).

To convert the AdvantageKit log file to a SysId-compatible format, follow the instructions below:

1. Open the AdvantageKit log file in AdvantageScope v3.0.2 or later. In the menu bar, go to "File" > "Export Data...".

2. Set the format to "WPILOG" and the timestamps to "AdvantageKit Cycles". For large log files, enter the prefixes for only the fields and tables necessary for SysId analysis (see the [export options](https://docs.advantagescope.org/more-features/export#options) documentation for details).

3. Click the save icon and choose a location to save the log.

4. Open the SysId analyzer by searching for "WPILib: Start Tool" in the VSCode command palette and choosing "SysId" (or using the desktop launcher on Windows). Open the exported log file by clicking "Open data log file..."

5. Choose the fields to analyze as normal.
