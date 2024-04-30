# Common Issues (And How To Avoid Them)

## Non-Deterministic Data Sources

AdvantageKit replay relies on all data sources being deterministic and synchronized. IO interfaces ensure this is the case for subsystems, and AdvantageKit automatically handles replay for core WPILib classes (`DriverStation`, `RobotController`, and `PowerDistribution`). However, it's easy to accidentally use data from sources that are not properly logged. **We recommend regularly testing out log replay during development to confirm that the replay outputs match the real outputs.** Spotting mistakes like this early is the key to fixing them before it becomes a critical issue at an event.

Some common non-deterministic data sources to watch out for include:

- NetworkTables data as inputs, including from driver dashboards. See [here](RECORDING-INPUTS.md#dashboard-options--networktables-inputs).
- Large hardware libraries like [YAGSL](https://github.com/BroncBotz3481/YAGSL) or [Phoenix 6 swerve](https://v6.docs.ctr-electronics.com/en/latest/docs/tuner/tuner-swerve/index.html), which interact with hardware directly instead of through an IO layer. Try using the AdvantageKit [swerve template project](INSTALLATION.md#new-projects) instead.
- Interactions with the RIO filesystem. Files can be saved and read by the robot code, but incoming data still needs to be treated as an input.
- Random number generation, which cannot be recreated in a simulator.
- Iteration over unordered collections (such as unordered maps).

## Multithreading For Replay

The main robot code logic must be single threaded to work with log replay. This is because the timing of extra threads cannot be recreated in simulation; threads will not execute at the same rate consistently, especially on different hardware.

There are two solutions to this issue:

- Threads are rarely required in FRC code, so start by considering alternatives. Control loop can often run great at 50Hz in the main robot thread, or consider using the closed-loop features of your preferred motor controller instead.
- If a thread is truly required, it must be isolated to an IO implementation. Since the inputs to the rest of the robot code are logged periodically, long-running tasks or high frequency control loops are possible (but as part of an IO implementation, they cannot be recreated during log replay).

> Note: PathPlanner includes a replanning feature that makes use of threads. The default implementation must be replaced with an AdvantageKit-compatible version as seen below. This class is included in AdvantageKit's [swerve template project](INSTALLATION.md#new-projects).

```java
Pathfinding.setPathfinder(new LocalADStarAK());
```

## Logging From Threads

AdvantageKit's logging APIs (i.e. `recordOutput` and `processInputs`) are **not** thread-safe, and should only be called from the main thread. As all values in AdvantageKit are synchronized to the main loop cycle, logging data from other threads would fail to capture values accurately even if this functionality was supported. Instead, threads should only be used in IO implementations (see above) and should record all values as inputs, synchronized appropriately to the main loop cycle in a thread-safe manner.

## Uninitialized Inputs

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

## Deterministic Timestamps

AdvantageKit's deterministic timestamps don't have significant effect on almost all FRC code. However, they may cause unexpected behavior for very advanced use cases. See [here](DATA-FLOW.md#deterministic-timestamps) for details.

## Unit Testing

Many units tests are unaffected by AdvantageKit's WPILib shims, but those which rely on data from `DriverStation` or `RobotController` (such as battery voltage) may not function as expected. This is because simulated inputs (as set by classes like `RoboRioSim`) are not updated outside of the periodic functions of `LoggedRobot`. To fix this, manually capture data and update the logging objects through a method like this:

```java
private void refreshAkitData() {
  ConduitApi.getInstance().captureData();
  LoggedDriverStation.getInstance().periodic();
  LoggedSystemStats.getInstance().periodic();
}
```

`refreshAkitData()` should be called after any updates to the simulated values managed by AdvantageKit.
