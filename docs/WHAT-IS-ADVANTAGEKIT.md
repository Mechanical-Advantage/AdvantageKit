# What is AdvantageKit?

## Background

A variety of logging frameworks already exist in FRC, from built-in tools like the Driver Station and [WPILib logging](https://docs.wpilib.org/en/stable/docs/software/telemetry/datalog.html) all the way to custom solutions for logging more detailed information. However, most systems exist around the same fundamental idea; record a limited set of values explicitly provided by the code. That could include sensor data, PID error, odometry pose, internal state, etc. While this has enormous value, it doesn't solve the eternal sentiment when something goes wrong: "If only we were logging one extra field!"

Logging in AdvantageKit is built around a different principle, often used in industry. Instead of logging specific output values from the user code, it records _all of the data flowing into the robot code_. Every sensor value, joystick press, NetworkTables update, and much more is logged every loop cycle. After a match, these values can be replayed to the robot code in a simulator. Since every input command is the same, all of the internal logic of the code is replayed exactly. This allows you to log extra fields after the fact, or add breakpoints and inspect the code line by line. This technique means that logging is more than just a tool for checking on specific issues; it's also a safety net that can be used to verify how any part of the code functions.

Logging in AdvantageKit is built with the following goals in mind:

- Support a high level of logging in a way that is **accessible**. This means reducing the changes required to user code as much as possible, and building on top of existing frameworks (like command-based WPILib).

- Provide **capable** and **easy-to-use** methods of viewing log data. AdvantageKit uses the WPILOG and NT4 formats for logs and live data streaming, which are supported both by WPILib tools and our viewer application [AdvantageScope](https://github.com/Mechanical-Advantage/AdvantageScope).

- Maintain **openness** and **transparency** during development as part of 6328's efforts with the [#OpenAlliance](https://github.com/OpenAllianceFRC/info).

## Example #1: Output Logging in Replay

Let's look at a concrete example of AdvantageKit in action. Below is a plot from 6328's 2022 robot, based on an AdvantageKit log file. It shows the flywheel's setpoint speed during a match.

![Setpoint speeds](resources/example-1.png)

During teleop, this speed is based on the distance between the robot and the target. We might want to check that the flywheel speed calculation is working correctly, but this log doesn't include the calculated distance that was used. **Let's use AdvantageKit to replay the log and produce this new data.**

First, we need to run the replay using the same version of code that generated this log. AdvantageKit can save metadata with the log file, and we've already stored the Git hash (uniquely identifying the version of code that was running on the robot):

![Viewing metadata](resources/example-2.png)

Next, we can return to the correct commit using git (`git checkout ab12cf`...) and add the following line to the odometry subsystem:

```java
Logger.recordOutput("TargetDistanceMeters", latestPose.getTranslation().getDistance(FieldConstants.hubCenter));
```

This line doesn't change the behavior of the robot code, but it logs extra "output" data every cycle. Recall that because...

- The **same code** is running on the robot and in the simulator AND...
- AdvantageKit will provide the **same inputs** to the simulator that occurred on the real robot...

...the outputs from the line we added are the same as if we were logging this data on the real robot. When we start the simulator, AdvantageKit loads the log file and replays the loop cycles as quickly as possible (most laptops are much faster than the roboRIO):

![Log replay](resources/example-3.gif)

AdvantageKit produced a new log file, which includes all of the data from the original plus a new "ReplayOutputs" table. All of these outputs match their counterparts in "RealOutputs" (logged by the robot on the field). For example, the odometry data (robot pose) was identical in replay:

![Comparing odometry replay](resources/example-4.gif)

The "ReplayOutputs" also includes our new field "TargetDistanceMeters". We can confirm that the flywheel setpoint is increasing along with distance, so the calculation is working correctly. Using replay, we're able to make that conclusion with complete certainty even though we're _using data that was never logged by the robot on the field._

![Setpoints speeds with distance](resources/example-5.png)

## Example #2: Bug Fixes in Replay

In addition to analyzing the robot code's behavior, we can also use replay to test improvements. 6328's 2022 robot used vision data from a Limelight to reset its odometry (calculated position on the field). This involves a complex pipeline in the robot code, and filtering invalid targets is critical. Below is a clip showing the robot's odometry during a particularly problematic moment. The green line indicates when vision data is being used to reset the position.

![Original odometry](resources/example-6.gif)

This movement is clearly impossible. Note that the Limelight on the robot is facing backwards, and couldn't possibly have detected the target at the center of the field when the position initially jumps across the field. Let's use the log data to dig deeper. The vision pipeline is based on the four detected corners of each piece of vision tape around the target. Plotting these points, we would expect to see a smooth curve like the image on the left. There are five pieces of tape with distinct corners. Instead, during this moment in the log we see the points to the right.

![Vision data](resources/example-7.png)

The Limelight isn't seeing the vision tape, so the corners are inaccurate. In fact, many of the targets it reports don't even have four corners! To improve the filtering, let's **check that the number of reported corners is divisible by four**:

```java
if (inputs.cornerX.length % 4 == 0 && inputs.cornerX.length == inputs.cornerY.length) {
    // Data looks good, continue processing
} else {
    // Bad data, discard this frame
}
```

After making this change, we run the code in replay and check the "ReplayOutputs". Unlike the first example, we've modified the original code and would expect the odometry to be different in replay. Here's the same clip as before, with the transluent robot representing the robot pose in replay:

![Replayed odometry](resources/example-8.gif)

The adjusted code now properly rejects the invalid target and accepts the valid one (notice that real and replay poses converge when the robot is pointed towards the real target). We can now run this code in future matches with confidence, because we know exactly what the code _would have done if this new version was running on the field._

## More Examples

6328 has discussed several examples of how we've used logging in our [#OpenAlliance 2022 build thread](https://www.chiefdelphi.com/t/frc-6328-mechanical-advantage-2022-build-thread). Here are some highlights:

- While working between shop meetings, we were able to debug a subtle issue with the code's vision pipeline by recording extra outputs from a log saved from a previous testing session. [More details here](https://www.chiefdelphi.com/t/frc-6328-mechanical-advantage-2022-build-thread/398645/21#logging).
- During an event, we retuned our odometry pipeline to more aggressively adjust based on vision data (tuning that requires a normal match environment with other robots). We tested the change using replay and deployed it in the next match, and saw much more reliable odometry data for the remainder of the event. [More details here](https://www.chiefdelphi.com/t/frc-6328-mechanical-advantage-2022-build-thread/398645/112#odometry-tuning).
- After an event, we determined that our hood was not zeroing currently during match conditions. This involved replaying the matches with a manually adjusted hood position and comparing the quality of the vision data processed with different angles (the robot's Limelight was mounted to the hood). [More details here](https://www.chiefdelphi.com/t/frc-6328-mechanical-advantage-2022-build-thread/398645/145#software-jonahb55).
- After adding a new point visualization tool to AdvantageScope, we replayed log files from a previous event and extracted more detailed information from the middle of the vision pipeline. [More details here](https://www.chiefdelphi.com/t/frc-6328-mechanical-advantage-2022-build-thread/398645/112#odometry-tuning).
- We used log data from across the full season to determine that the FMS adds ~300ms of extra time to auto and teleop (rather than following a strict 15s and 135s). [More details here](https://www.chiefdelphi.com/t/frc-6328-mechanical-advantage-2022-build-thread/398645/112#how-long-is-a-match)
- We replayed our Einstein matches while pulling in Zebra data to check the accuracy of the on-robot odometry. [More details here](https://www.chiefdelphi.com/t/frc-6328-mechanical-advantage-2022-build-thread/398645/134#zebra-analysis)
- We compiled log data from the robot's full lifetime to generate some fun statistics. For example, the total energy usage of the robot was equivalent to the potential energy of a grand piano dropped from an airplane at cruising altitude. [More details here](https://www.chiefdelphi.com/t/frc-6328-mechanical-advantage-2022-build-thread/398645/134#software-update-9-beware-of-falling-pianos).
