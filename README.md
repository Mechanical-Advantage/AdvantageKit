# ![AdvantageKit](/banner.png)

[![Build](https://github.com/Mechanical-Advantage/AdvantageKit/actions/workflows/build.yml/badge.svg?branch=main&event=push)](https://github.com/Mechanical-Advantage/AdvantageKit/actions/workflows/build.yml)

AdvantageKit is a logging, telemetry, and replay framework developed by Team 6328. Please see the following guides to get started, or the README files for individual components.

- [What is AdvantageKit?](/docs/WHAT-IS-ADVANTAGEKIT.md)
- [Installing AdvantageKit](/docs/INSTALLATION.md)
- [Understanding Data Flow](/docs/DATA-FLOW.md)
- [Code Structure & IO Layers](/docs/CODE-STRUCTURE.md)
- [Developing AdvantageKit](/docs/DEVELOPING.md)

## Example Projects

Looking to get started quickly? Here are some example projects to check out:

- [AdvantageKit Skeleton Template](https://github.com/Mechanical-Advantage/AdvantageKit/releases/latest) - Simple `TimedRobot` style template project. The zip is attached to the latest release.
- [AdvantageKit Command-Based Example](https://github.com/Mechanical-Advantage/AdvantageKit/releases/latest) - Example command based project with a tank drive and closed-loop flywheel, including sim support. The zip is attached to the latest release.
- [Team 6328's 2022 Swerve Code](https://github.com/Mechanical-Advantage/SwerveDevelopment) - Swerve drive code built with AdvantageKit v2 (2023).
- [Team 6328's 2022 Competition Code](https://github.com/Mechanical-Advantage/RobotCode2022) - Full robot code built with AdvantageKit v1 (2022).
- [Team 4099's 2022 Competition Code](https://github.com/team4099/RapidReact-2022) - Kotlin robot code built with AdvantageKit v1 (2022).

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
