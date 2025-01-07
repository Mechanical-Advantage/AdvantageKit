---
sidebar_position: 1
---

# Full Changelog

### Library: Core

- Implemented logging and replay without WPILib shims
- Integrated with RobotController timestamp setter
- Removed `disableDeterministicTimestamps` method ([docs](/data-flow/deterministic-timestamps))
- Switched system stats and power distribution logging to output only ([docs](/data-flow/built-in-logging#power-distribution-data))
- Switched to LoggedMechanism2d class for mechanism logging ([docs](/data-flow/supported-types#mechanisms-output-only))
- Added logging for additional periodic performance stats ([docs](/data-flow/built-in-logging#performance-data))
- Added check for `LoggedRobot` when calling `Logger.start()`
- Synchronously read DS data in conduit
- Removed WPILib version check
- Added support for WPILib 2025
- Switched to WPILib maven for distribution

### Library: Features

- Added replay watch feature ([docs](/getting-started/replay-watch), [example](/getting-started/what-is-advantagekit/example-rapid-iteration))
- Added record logging with structs ([docs](/data-flow/supported-types#records))
- Added support for 2D array logging ([docs](/data-flow/supported-types#simple))
- Added support for primitive supplier logging as outputs ([docs](/data-flow/supported-types#suppliers-output-only))
- Added support for recursive input logging
- Added automatic alerts logging ([docs](/data-flow/built-in-logging#alerts))
- Added automatic logging for VH-109 radio ([docs](/data-flow/built-in-logging#radio-status))
- Added new network input classes for more generic dashboard logging ([docs](/data-flow/recording-inputs/dashboard-inputs))
- Added numeric indices for replay path suffixes
- Automatically open replay logs in AdvantageScope
- Added support for 2025 URCL
- Fixed `@AutoLogOutput` for `Measure` objects

### Library: Miscellaneous

- Added new format for default filenames (start with "akit" prefix and include event name)
- Exposed method to enable manual object scanning for `@AutoLogOutput`
- Moved WPILOG flush to AdvantageKit thread
- Improved log close behavior in replay
- Improved warnings for protobuf and record logging
- Removed deprecated `LogTable` getters
- Removed deprecated `Logger.getInstance()` method
- Changed user code time for the first cycle to include the `Robot` constructor
- Added `[AdvantageKit]` prefix to all console message
- Added `AdvantageKit_` prefix to all thread names

### Template Projects & Documentation

- Added new differential drive project ([docs](/getting-started/template-projects/diff-drive-template))
- Added new Spark swerve project ([docs](/getting-started/template-projects/spark-swerve-template))
- Added new TalonFX swerve project ([docs](/getting-started/template-projects/talonfx-swerve-template))
- Added new vision template project ([docs](/getting-started/template-projects/vision-template))
- Cleaned up all template projects
- Added documentation for all template projects
- Reorganized all documentation
- Added search to docs
- Added log replay comparison docs ([link](/getting-started/what-is-advantagekit/log-replay-comparison))
- Updated recommended auto-commit system
- Switched recommended setup location from `robotInit` to the constructor of `Robot`
