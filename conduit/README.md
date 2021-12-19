# `conduit`

> Transfers data between `junction` and the WPILib HAL efficiently.

`conduit` includes both Java and C++ components. [`wpilibio`](/conduit/wpilibio) (C++) reads data directly from the HAL and transfers it to [`api`](/conduit/api) (Java) using a shared memory buffer. See [here](/docs/CONDUIT-SHIMS.md) for more details on the purpose of `conduit`.

The format of the shared buffer is defined using [flatbuffers](https://google.github.io/flatbuffers/). See [conduit_schema.fbs](conduit_schema.fbs) for details.

## Components

* [`api`](/conduit/api) - Java interface for `wpilibio`, transfers data from the shared memory buffer.
* [`wpilibio`](/conduit/wpilibio) - Reads data from the HAL and manages the shared memory buffer.

## Interface

`junction` interacts with [`api`](/conduit/api) to read data from `conduit`. User code does not use c`onduit` directly.