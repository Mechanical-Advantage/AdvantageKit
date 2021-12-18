# Repository Structure

AdvantageKit contains a variety of projects and tools, listed below.

## Logging

* [junction](/junction) - Primary component of logging, manages the flow of data between user code, WPILib, log files, network clients, etc. *Written in Java.*

  * [junction/core](/junctionc/core) - Central system for managing data, including reading and writing from log files and user code.

  * [junction/shims](/junctionc/shims) - Replaces components of other libraries (WPILib) to interact directly with `junction`.

    * [junction/shims/wpilib](/junctionc/shims/wpilib) - Replaces WPILib components to read data from `junction` instead of the HAL.

* [conduit](/conduit) - Transfers data between junction and the WPILib HAL efficiently. *Written in C++ and Java.*

* [example_logs](/example_logs) - Example RLOG files for testing.

## General

* [build_tools](/build_tools) - Utilities to assist with building in Bazel and interfacing with WPILib and the roboRIO.

* [third_party](/third_party) - Tools for integrating third party libraries like WPILib.