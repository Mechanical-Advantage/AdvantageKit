# ![AdvantageKit](/banner.png)

[![Build](https://github.com/Mechanical-Advantage/AdvantageKit/actions/workflows/build.yml/badge.svg?branch=main&event=push)](https://github.com/Mechanical-Advantage/AdvantageKit/actions/workflows/build.yml)

AdvantageKit is a logging, telemetry, and replay framework developed by Team 6328. AdvantageKit enables **log replay**, where the full state of the robot code can be replayed in simulation based on a log file ([What is AdvantageKit?](/docs/WHAT-IS-ADVANTAGEKIT.md)). See also:

- [AdvantageScope](https://github.com/Mechanical-Advantage/AdvantageScope), our robot telemetry application which **does not require AdvantageKit to use**.
- [WPILib Data Logging](https://docs.wpilib.org/en/stable/docs/software/telemetry/datalog.html), a simpler logging system included in WPILib (does not support log replay in simulation, but covers the needs of most teams).

Please see the following guides to get started with AdvantageKit:

- [What is AdvantageKit?](/docs/WHAT-IS-ADVANTAGEKIT.md)
- [Installation](/docs/INSTALLATION.md)
- [Data Flow](/docs/DATA-FLOW.md)
- [Recording Inputs: IO Layers](/docs/RECORDING-INPUTS.md)
- [Recording Outputs](/docs/RECORDING-OUTPUTS.md)
- [Version Control](/docs/VERSION-CONTROL.md)
- [Common Issues (And How To Avoid Them)](/docs/COMMON-ISSUES.md)
- [Developing AdvantageKit](/docs/DEVELOPING.md)

Feedback, feature requests, and bug reports are welcome on the [issues page](https://github.com/Mechanical-Advantage/AdvantageKit/issues). For non-public inquires, please send a message to software@team6328.org.

## Sample Projects

Looking to get started quickly? Here are some example projects to check out:

- [AdvantageKit Example Projects](https://github.com/Mechanical-Advantage/AdvantageKit/blob/main/docs/INSTALLATION.md#new-projects) - Example projects for differential and swerve drives, from minimal to advanced (including a project designed for the [2024 FIRST KitBot](https://www.firstinspires.org/resource-library/frc/kitbot)). Zip files for each project are attached to the latest release.
- [Team 6328's 2023 Competition Code](https://github.com/Mechanical-Advantage/RobotCode2023) - Java robot code built with AdvantageKit v2 (2023).
- [Team 2910's 2023 Competition Code](https://github.com/FRCTeam2910/2023CompetitionRobot-Public) - Java robot code built with AdvantageKit v2 (2023).
- [Team 4099's 2023 Competition Code](https://github.com/team4099/ChargedUp-2023/tree/main) - Kotlin robot code built with AdvantageKit v2 (2023).
- [Team 6328's 2022 Competition Code](https://github.com/Mechanical-Advantage/RobotCode2022) - Java robot code built with AdvantageKit v1 (2022).

## Repository Structure

### Logging

- [junction](/junction) - Primary component of logging, manages the flow of data between user code, WPILib, log files, network clients, etc. _Written in Java._
  - [junction/core](/junction/core) - Central system for managing data, including reading and writing from log files and user code.
  - [junction/shims](/junction/shims) - Replaces components of other libraries (WPILib) to interact directly with `junction`.
    - [junction/shims/wpilib](/junction/shims/wpilib) - Replaces WPILib components to read data from `junction` instead of the HAL.
  - [junction/autolog](/junction/autolog) - Annotation procesor for creating log input classes.
- [conduit](/conduit) - Transfers data between `junction` and the WPILib HAL efficiently. _Written in C++ and Java._

### General

- [build_tools](/build_tools) - Utilities to assist with building in Bazel and interfacing with WPILib and the roboRIO.
- [third_party](/third_party) - Tools for integrating third party libraries like WPILib.
