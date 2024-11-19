# ![AdvantageKit](/docs/docs/img/banner.png)

[![Build](https://github.com/Mechanical-Advantage/AdvantageKit/actions/workflows/build.yml/badge.svg?branch=main&event=push)](https://github.com/Mechanical-Advantage/AdvantageKit/actions/workflows/build.yml)

AdvantageKit is a logging, telemetry, and replay framework developed by [Team 6328](https://littletonrobotics.org). AdvantageKit enables **log replay**, where the full state of the robot code can be replayed in simulation based on a log file ([What is AdvantageKit?](https://docs.advantagekit.org/what-is-advantagekit/)). See also:

- [AdvantageScope](https://github.com/Mechanical-Advantage/AdvantageScope), our robot telemetry application which **does not require AdvantageKit to use**.
- [WPILib Data Logging](https://docs.wpilib.org/en/stable/docs/software/telemetry/datalog.html), a simpler logging system included in WPILib (does not support log replay in simulation, but covers the needs of most teams).

**View the [online documentation](https://docs.advantagekit.org).**

Feedback, feature requests, and bug reports are welcome on the [issues page](https://github.com/Mechanical-Advantage/AdvantageKit/issues). For non-public inquires, please send a message to software@team6328.org.

## Sample Projects

Looking to get started quickly? **Check out the [AdvantageKit example projects](https://docs.advantagekit.org/installation/#new-projects).** There are versions for differential and swerve drives, from minimal to advanced (including a project designed for the [2024 FIRST KitBot](https://www.firstinspires.org/resource-library/frc/kitbot)). Zip files for each project are attached to the latest release.

Also take a look at the examples below of teams utilizing AdvantageKit in their competition code:

- [Team 6328's 2023 Code](https://github.com/Mechanical-Advantage/RobotCode2023) - Spark Max swerve with a triple jointed arm, pose estimation, and auto scoring ([TBA](https://www.thebluealliance.com/team/6328/2023)).
- [Team 2910's 2023 Code](https://github.com/FRCTeam2910/2023CompetitionRobot-Public) - Talon FX swerve with PathPlanner, vision, and a telescoping arm ([TBA](https://www.thebluealliance.com/team/2910/2023)).
- [Team 3476's 2023 Code](https://github.com/FRC3476/FRC-2023) - Swerve with pose estimation, custom superstructure management, and more ([TBA](https://www.thebluealliance.com/team/3476/2023)).
- [Team 5940's 2023 Code](https://github.com/BREAD5940/2023-Onseason) - Talon FX swerve with automated scoring/pickup and "Northstar" AprilTag vision system ([TBA](https://www.thebluealliance.com/team/5940/2023))
- [Team 4099's 2023 Code](https://github.com/team4099/ChargedUp-2023/tree/main) - Kotlin code with a swerve, elevator, pose estimation, etc ([TBA](https://www.thebluealliance.com/team/4099/2023)).
- [Team 6328's 2022 Code](https://github.com/Mechanical-Advantage/RobotCode2022) - Spark Max differential drive with pose estimation and auto aiming ([TBA](https://www.thebluealliance.com/team/6328/2022)).

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
