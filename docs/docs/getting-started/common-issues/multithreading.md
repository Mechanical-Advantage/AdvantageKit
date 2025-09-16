---
sidebar_position: 2
---

# Multithreading

The main robot code logic must be single-threaded to work with log replay. This is because the timing of extra threads cannot be recreated in simulation; threads will not execute at the same rate consistently, especially on different hardware.

There are two solutions to this issue:

- Threads are rarely required in FRC code, so start by considering alternatives. Control loop can often run great at 50Hz in the main robot thread, or consider using the closed-loop features of your preferred motor controller instead.
- If a thread is truly required, it must be isolated to an IO implementation. Since the inputs to the rest of the robot code are logged periodically, long-running tasks or high-frequency control loops are possible (but as part of an IO implementation, they cannot be recreated during log replay).

## Logging From Threads

AdvantageKit's logging APIs (i.e. `recordOutput` and `processInputs`) are **not** thread-safe, and should only be called from the main thread. As all values in AdvantageKit are synchronized to the main loop cycle, logging data from other threads would fail to capture values accurately even if this functionality was supported. Instead, threads should only be used in IO implementations (see above) and should record all values as inputs, synchronized appropriately to the main loop cycle in a thread-safe manner.
