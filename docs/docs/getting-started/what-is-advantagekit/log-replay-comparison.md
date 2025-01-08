---
sidebar_position: 5
---

# Comparison of Log Replay Tools

Several logging tools are available to FRC teams with different capabilities and use cases. This page compares non-replay logging tools to several replay-capable logging tools, including AdvantageKit.

:::info
Skip to the [summary section](#summary) to compare the features of each logging tool.
:::

### Non-Replay Logging

_(DataLogManager, Epilogue, Monologue, etc.)_

These tools allow for logging and/or live streaming of data published from robot code, which can be viewed using AdvantageScope. This is sufficient for the majority of debugging tasks, such as verifying pose estimation accuracy, checking joystick values, and debugging simple issues in code that was configured for logging ahead of time. However, these tools **do not support replaying data to simulated robot code** (as explained [here](/getting-started/what-is-advantagekit)).

### CTRE Signal Logging & Hoot Replay

CTRE's [signal logging](https://v6.docs.ctr-electronics.com/en/stable/docs/api-reference/api-usage/signal-logging.html) system for Phoenix 6 automatically records CAN data from supported devices. For 2025, CTRE introduced the [Hoot Replay](https://v6.docs.ctr-electronics.com/en/latest/docs/yearly-changes/yearly-changelog.html#hoot-replay) feature that allows data from a single Hoot log to be replayed to robot code during simulation. This enables **simple debugging** of code under the following circumstances:

- Code logic is dependent **only on data from logged devices** and/or custom signals (if manually integrated by the user).
- Code logic is **not affected** by **joystick inputs**, **non-CAN devices**, **NetworkTables values** (such as vision measurements or dashboard choosers), or other non-replayed values.
- Code logic **does not depend on the timing of signals** (such as in which loop cycle a signal arrives).

:::warning
Hoot Replay is **non-deterministic**, which means that **no guarantee is made that replayed robot code will match the behavior of the real robot**. It is best used when debugging issues of limited scope that are highly dependent on Phoenix APIs.

The accuracy of Hoot Replay is **affected by the speed of replay**, which limits its utility when running faster than real-time or rapidly iterating on code.
:::

Hoot logging is enabled by default and **does not require architecture changes to code**. This enables replay of simple logic (such as non-vision odometry) without requiring any code changes. However, replaying high-level logic dependent on non-Phoenix data (such as joysticks or vision inputs) may require code architecture changes to log and replay custom user signals.

### AdvantageKit

AdvantageKit guarantees **deterministic replay of robot code in simulation**, allowing for [additional fields to be logged](./example-output-logging.md) or [code logic to be adjusted](./example-bug-fixes.md) in replay with complete certainty of the original and adjusted behavior of the real robot. AdvantageKit **guarantees that the replayed robot code will match the behavior of the real robot**. Deterministic replay enables several important features of AdvantageKit:

- Debug in replay with complete trust that **changes will be reflected as intended** on the real robot.
- Replay logs at **any speed** without losing accuracy. This allows entire log files to be replayed many times faster than real-time, allowing for [**rapid iteration and testing**](./example-rapid-iteration.md) in replay. Plus, [Replay Watch](../replay-watch.md) allows code to be **replayed automatically without needing to repeatedly launch simulation**.
- AdvantageKit automatically **merges original and replayed fields** in the same output log file, allowing for easy comparison. Replayed logs are **automatically opened in AdvantageScope.**
- Breakpoints can be used to pause replay and **inspect code line-by-line** with no impact on replay accuracy.

AdvantageKit is not enabled by default. Code bases not already using [hardware abstraction](/data-flow/recording-inputs/io-interfaces) for subsystems must be restructured when switching to AdvantageKit.

:::info
AdvantageKit is **free** and **open-source**. It can be used with hardware from any vendor, including CTRE, REV, and more.
:::

## Summary

|                                                                                                                                                                 | Non-Replay                         | CTRE/Hoot (Nondeterministic)                                                            | AdvantageKit (Deterministic) |
| --------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------- | --------------------------------------------------------------------------------------- | ---------------------------- |
| Record data viewable in AdvantageScope                                                                                                                          | <center>✅</center>                | <center>✅</center>                                                                     | <center>✅</center>          |
| Replay of <u>enabled and autonomous state</u>                                                                                                                   | <center>❌</center>                | <center>✅</center>                                                                     | <center>✅</center>          |
| <u>Low-level replay</u> of code using Phoenix APIs                                                                                                              | <center>❌</center>                | <center>✅[^1]</center>                                                                 | <center>❌</center>          |
| **Guarantee of <u>replay accuracy</u>**<br /><sub>See previous pages for full examples</sub>                                                                    | <center>❌</center>                | <center>❌</center>                                                                     | <center>✅</center>          |
| Replay of all <u>Driver Station data</u><br /><sub>Full robot mode, joystick inputs, etc.</sub>                                                                 | <center>❌</center>                | <center>❌</center>                                                                     | <center>✅</center>          |
| Replay of <u>FPGA timestamps</u><br /><sub>Used by WPILib, commands, etc.</sub>                                                                                 | <center>❌</center>                | <center>❌</center>                                                                     | <center>✅</center>          |
| Replay at <u>any speed</u> without losing accuracy<br /><sub>Enables efficient replay of entire log files</sub>                                                 | <center>❌</center>                | <center>❌</center>                                                                     | <center>✅</center>          |
| Save original and replayed values to the <u>same log file</u><br /><sub>Enables easy comparison of fields</sub>                                                 | <center>❌</center>                | <center>❌</center>                                                                     | <center>✅</center>          |
| Iterate rapidly on code with <u>[Replay Watch](../replay-watch.md)</u><br /><sub>Quickly tune pose estimation, sensor filtering, control logic, and more!</sub> | <center>❌</center>                | <center>❌</center>                                                                     | <center>✅</center>          |
| Debug replayed code using <u>breakpoints</u>                                                                                                                    | <center>❌</center>                | <center>⚠️[^2]</center>                                                                 | <center>✅</center>          |
| Replay <u>complex code logic</u><br /><sub>Commands, autos, pose estimation, etc.</sub>                                                                         | <center>❌</center>                | <center>⚠️[^3]</center>                                                                 | <center>✅</center>          |
| Replay code using <u>any vendor library</u><br /><sub>Phoenix, REVLib, Limelight, etc.</sub>                                                                    | <center>❌</center>                | <center>⚠️[^3]</center>                                                                 | <center>✅</center>          |
| Replay code using <u>non-CAN inputs</u><br /><sub>Vision, analog inputs, DIOs, etc.</sub>                                                                       | <center>❌</center>                | <center>⚠️[^3]</center>                                                                 | <center>✅</center>          |
| FRC language support                                                                                                                                            | <center>Java, Python, C++</center> | <center>Java, Python, C++</center>                                                      | <center>Java</center>        |
| Pricing                                                                                                                                                         | <center>Free</center>              | <center>Requires [Phoenix Pro](https://store.ctr-electronics.com/phoenix-pro/)</center> | <center>Free</center>        |

[^1]: Code logic replayed using Hoot Replay may not match the behavior of the real robot. Phoenix timestamps available during Hoot Replay use a different time base than the real robot.
[^2]: Use of breakpoints with Hoot Replay requires careful use of step timing or play/pause logic to ensure similarity to the original behavior. AdvantageKit replay can be paused, slowed down, etc. with no effect on guarantees of replay accuracy.
[^3]: Use of non-Phoenix values requires manual implementation of logging and replay logic, including conversion of time bases and custom code architecture supporting hardware abstraction (or similar). Nondeterministic replay makes no guarantee that replayed code will match the behavior of the real robot.
