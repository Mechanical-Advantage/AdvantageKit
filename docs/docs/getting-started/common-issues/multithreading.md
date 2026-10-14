---
sidebar_position: 2
---

# Multithreading {#multithreading}

The main robot code logic must be single-threaded to work with log replay. This is because the timing of extra threads cannot be recreated in simulation; threads will not execute at the same rate consistently, especially on different hardware.

There are two solutions to this issue:

- Threads are rarely required in FRC code, so start by considering alternatives. Control loop can often run great at 50Hz in the main robot thread, or consider using the closed-loop features of your preferred motor controller instead.
- If a thread is truly required, it must be isolated to an IO implementation. Since the inputs to the rest of the robot code are logged periodically, long-running tasks or high-frequency control loops are possible (but as part of an IO implementation, they cannot be recreated during log replay).

## Logging From Threads {#logging-from-threads}

AdvantageKit's `recordOutput` API is thread-safe. Outputs logged from background threads are safely queued and recorded with the active loop cycle. This allows background threads (such as vision coprocessor communication or asynchronous path generation) to record telemetry and debug data without manually piping it back to the main thread.

However, `processInputs` is **not** thread-safe and must only be called from the main thread. As inputs define the boundary between IO implementations and deterministic robot logic, recording inputs from secondary threads would break log replay synchronization. If `processInputs` is invoked from a non-main thread, the call is ignored (no-op) and a warning is printed to the Driver Station.
