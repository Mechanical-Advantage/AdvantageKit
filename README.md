# AdvantageKit

[![Build](https://github.com/Mechanical-Advantage/AdvantageKit/actions/workflows/build.yml/badge.svg?branch=main&event=push)](https://github.com/Mechanical-Advantage/AdvantageKit/actions/workflows/build.yml)

AdvantageKit is a collection of frameworks, tools, and utilities by Team 6328 for developing robot code. Please see the following guides to get started, or the README files for individual components:

* [Getting Started: Logging](/docs/START-LOGGING.md)
* [Getting Started: Developing AdvantageKit](/docs/START-DEVELOPING.md)
* [Conduit & WPILib Shims](/docs/CONDUIT-SHIMS.md)
* [RLOG Specification](/docs/RLOG-SPEC.md)

## Repository Structure

### Logging

* [junction](/junction) - Primary component of logging, manages the flow of data between user code, WPILib, log files, network clients, etc. *Written in Java.*

  * [junction/core](/junction/core) - Central system for managing data, including reading and writing from log files and user code.

  * [junction/shims](/junction/shims) - Replaces components of other libraries (WPILib) to interact directly with `junction`.

    * [junction/shims/wpilib](/junction/shims/wpilib) - Replaces WPILib components to read data from `junction` instead of the HAL.

* [conduit](/conduit) - Transfers data between `junction` and the WPILib HAL efficiently. *Written in C++ and Java.*

* [example_logs](/example_logs) - Example RLOG files for testing.

### General

* [build_tools](/build_tools) - Utilities to assist with building in Bazel and interfacing with WPILib and the roboRIO.

* [third_party](/third_party) - Tools for integrating third party libraries like WPILib.