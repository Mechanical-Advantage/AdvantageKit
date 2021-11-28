# Repository Structure

AdvantageKit contains a variety of projects and tools, listed below.

## Logging

* [junction](/junction) - Primary component of logging, manages the flow of data between user code, WPILib, log files, network clients, etc. Written in Java.

* [conduit](/conduit) - Transfers data between junction and the WPILib HAL efficiently. Written in C++ and Java.

* [example_logs](/example_logs) - Example RLOG files for testing.

## General

* [build_tools](/build_tools) - Utilities to assist with building in Bazel and interfacing with WPILib and the roboRIO.

* [third_party](/third_party) - Tools for integrating third party libraries like WPILib.