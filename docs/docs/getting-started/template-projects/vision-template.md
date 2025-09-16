---
sidebar_position: 5
---

# Vision Template

The vision template project provides a starting point for creating a high-performance vision or pose estimation system compatible with AdvantageKit's deterministic log replay. It includes support for the following features:

- Integration with both Limelight and PhotonVision
- Vision simulation via PhotonLib
- Options for both simple targeting and full pose estimation
- High-frequency sampling to ensure that every observation is processed (and never duplicated)
- Efficient logging of observations via structs
- Advanced filtering options, including automatic scaling of standard deviations
- Detailed logging of filters, allowing for easy tuning in replay
- **Deterministic replay** with a **guarantee of accuracy**

:::info
The AdvantageKit vision template is **open-source** and **fully customizable**:

- **No black boxes:** Users can view and adjust all layers of the vision processing stack.
- **Customizable:** IO implementations can be adjusted to support any hardware configuration.
- **Replayable:** Every aspect of the vision processing and filtering logic can be replayed and logged in simulation using AdvantageKit's deterministic replay features with _guaranteed accuracy_.

:::

## ⚠️ Warning

This project is provided as a **starting point** that will work reasonably well across a variety of situations, but **must be customized to fit your specific needs**. It is intended as a **platform** on top of which more optimized systems can be designed. The best pose estimation systems account for a wide variety of factors, including:

- Game design
- Robot design
- Strategic objectives
- Field tolerances
- ...and more!

High-quality pose estimation requires frequent iteration of all aspects of the control stack to address these factors, including **cameras, mounts, coprocessors, calibrations, pipelines, communication, and filtering**. This project provides a starting point for only one small part of a well-optimized vision stack.

## Configuration

The project is primarily configured via the `VisionConstants` class, with comments explaining the purpose of each field. The selected vision implementation can be changed in the constructor of `RobotContainer`.

:::warning
The AprilTag layout defined the top of `VisionConstants` must be updated when using the AndyMark field layout, and should match the field layout configured in [AdvantageScope](https://docs.advantagescope.org/tab-reference/3d-field), [Limelight](https://docs.limelightvision.io/docs/docs-limelight/pipeline-apriltag/apriltag-map-specification), and/or [PhotonVision](https://docs.photonvision.org/en/latest/docs/apriltag-pipelines/multitag.html#updating-the-field-layout). **Please see [Team Update 12](https://firstfrc.blob.core.windows.net/frc2025/Manual/TeamUpdates/TeamUpdate12.pdf) for details.**
:::

:::tip
In addition to pose estimation, this project include an example of simple targeting with AprilTags. For many games and robots, this is a significantly simpler method of accomplishing game objectives. Check the `getTargetX` method of `Vision` and `configureButtonBindings` method of `RobotContainer` for details.
:::

### Logging

The vision subsystem logs a large set of outputs that can be used for debugging and tuning. Each camera logs the following fields:

- `TagPoses`: A list of 3D poses representing the set of visible tags. We recommend visualizing this field using the "Vision Target" object on the 3D field tab in AdvantageScope.
- `RobotPoses`: A list of 3D poses representing the raw pose estimates from the last cycle. We recommend visualizing this field using the "Ghost" object on the 3D field tab in AdvantageScope.
- `RobotPosesAccepted`: A subset of the `RobotPoses` list with the set of estimates that passed all stages of filtering.
- `RobotPosesRejected`: A subset of the `RobotPoses` list with the set of estimates that were removed during filtering.

The `Summary` table includes identical fields which include samples from every camera.

### Limelight 4

This project is compatible with all variants of Limelight by default (in addition to PhotonVision). **Limelight 4** users who wish to take advantage of the built-in IMU for MegaTag 2 should check the [Limelight documentation](https://docs.limelightvision.io/docs/docs-limelight/pipeline-apriltag/apriltag-robot-localization-megatag2#using-limelight-4s-built-in-imu-with-imumode_set--setimumode) for details. Note that the template already publishes the robot orientation every loop cycle.

:::info
Users can configure the IMU mode by importing [LimelightLib](https://docs.limelightvision.io/docs/docs-limelight/apis/limelight-lib) or by publishing an integer to the `imumode_set` key in NetworkTables.
:::

### Real-Time Thread Priority

Optionally, the main thread can be configured to use [real-time](https://blogs.oracle.com/linux/post/task-priority) priority when running the command scheduler by removing the comments [here](https://github.com/Mechanical-Advantage/AdvantageKit/blob/a86d21b27034a36d051798e3eaef167076cd302b/template_projects/sources/vision/src/main/java/frc/robot/Robot.java#L90) and [here](https://github.com/Mechanical-Advantage/AdvantageKit/blob/a86d21b27034a36d051798e3eaef167076cd302b/template_projects/sources/vision/src/main/java/frc/robot/Robot.java#L100) (**IMPORTANT:** You must uncomment _both_ lines). This may improve the consistency of loop cycle timing in some cases, but should be used with caution as it will prevent other threads from running during the user code loop cycle (including internal threads required by NetworkTables, vendors, etc).

This customization **should only be used if the loop cycle time is significantly less than 20ms**, which allows other threads to continue running between user code cycles. We always recommend **thoroughly testing this change** to ensure that it does not cause unintended side effects (examples include NetworkTables lag, CAN timeouts, etc). In general, **this customization is only recommended for advanced users** who understand the potential side-effects.
