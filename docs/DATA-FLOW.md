# Understanding Data Flow

`Logger` is the primary class managing data flow for AdvantageKit. It functions in two possible modes depending on the environment:

- **Real robot/simulator** - When running on a real robot (or a physics simulation, Romi, etc.), `Logger` reads data from the user program and built-in sources, then saves it to one or more targets (usually a log file).
- **Replay** - During this mode, which runs in the simulator, `Logger` reads data from an external source like a log file and writes it out to the user program. It then records the original data (plus any outputs from the user program) to a separate log file.

![Diagram of data flow](resources/data-flow.png)

Below are definitions of each component:

- **User inputs** - Input data from hardware managed by the user program. This primarily includes input data to subsystem classes. See ["Code Structure"](CODE-STRUCTURE.md) for how this component is implemented.
- **User outputs** - Data produced by the user program based on the current inputs (odometry, calculated voltages, internal states, etc.). This data can be reproduced during replay, so it's the primary method of debugging code based on a log file. _Starting in 2023, user outputs also include any text sent to the console (logged automatically by AdvantageKit)._
- **Replay source** - Provides data from an external source for use during replay. This usually means reading data from a log file produced by the robot. A replay source only exists while in replay (never on the real robot).
- **Data receiver** - Saves data to an external source in all modes. Multiple data receivers can be provided (or none at all). While data receivers can to a log file or send data over the network.
- **LoggedDriverStation** _(Built-in input)_ - Internal class for recording and replaying driver station data (enabled state, joystick data, alliance color, etc).
- **LoggedSystemStats** _(Built-in input)_ - Internal class for recording and replaying data from the roboRIO (battery voltage, rail status, CAN status).
- **LoggedPowerDistribution** _(Built-in input)_ - Internal class for recording and replaying data from the PDP or PDH (channel currents, faults, etc).

Data is stored using string keys where slashes are used to denote subtables (similar to NetworkTables). Each subsystem stores data in a separate subtable. Like NetworkTables, **all logged values are persistent (they will continue to appear on subsequent cycles until updated**). The following data types are currently supported:

`boolean, long, float, double, String, boolean[], long[], float[], double[], String[], byte[]`

`Logger` is also responsible for managing timestamps. The current timestamp from the FPGA is read at the start of each cycle and synchronized for all logic. For example, calls to `Timer.getFPGATimestamp()` or `WPIUtilJNI.now()` will return this synchronized timestamp. This system guarantees that control logic can be replayed accurately.
