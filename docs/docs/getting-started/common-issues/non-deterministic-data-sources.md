---
sidebar_position: 1
---

# Non-Deterministic Data Sources

AdvantageKit replay relies on all data sources being deterministic and synchronized. IO interfaces ensure this is the case for subsystems, and AdvantageKit automatically handles replay for core WPILib classes (`DriverStation`, `RobotController`, and `PowerDistribution`). However, it's easy to accidentally use data from sources that are not properly logged. **We recommend regularly testing out log replay during development to confirm that the replay outputs match the real outputs.** Spotting mistakes like this early is the key to fixing them before it becomes a critical issue at an event.

Some common non-deterministic data sources to watch out for include:

- NetworkTables data as inputs, including from driver dashboards. See [here](/recording-inputs/dashboard-inputs).
- Large hardware libraries like [YAGSL](https://github.com/BroncBotz3481/YAGSL) or [Phoenix 6 swerve](https://v6.docs.ctr-electronics.com/en/latest/docs/tuner/tuner-swerve/index.html), which interact with hardware directly instead of through an IO layer. Try using the AdvantageKit [swerve template project](/installation#new-projects) instead.
- Interactions with the RIO filesystem. Files can be saved and read by the robot code, but incoming data still needs to be treated as an input.
- Random number generation, which cannot be recreated in a simulator.
- Iteration over unordered collections (such as unordered maps).
