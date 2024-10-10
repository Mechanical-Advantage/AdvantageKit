---
sidebar_position: 4
---

# Unit Testing

Many units tests are unaffected by AdvantageKit's WPILib shims, but those which rely on data from `DriverStation` or `RobotController` (such as battery voltage) may not function as expected. This is because simulated inputs (as set by classes like `RoboRioSim`) are not updated outside of the periodic functions of `LoggedRobot`. To fix this, manually capture data and update the logging objects through a method like this:

```java
private void refreshAkitData() {
  ConduitApi.getInstance().captureData();
  LoggedDriverStation.getInstance().periodic();
  LoggedSystemStats.getInstance().periodic();
}
```

`refreshAkitData()` should be called after any updates to the simulated values managed by AdvantageKit.
