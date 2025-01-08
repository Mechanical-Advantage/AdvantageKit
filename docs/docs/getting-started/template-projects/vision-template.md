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

:::tip
In addition to pose estimation, this project include an example of simple targeting with AprilTags. For many games and robots, this is a significantly simpler method of accomplishing game objectives. Check the `getTargetX` method of `Vision` and `configureButtonBindings` method of `RobotContainer` for details.
:::

The vision subsystem logs a large set of outputs that can be used for debugging and tuning. Each camera logs the following fields:

- `TagPoses`: A list of 3D poses representing the set of visible tags. We recommend visualizing this field using the "Vision Target" object on the 3D field tab in AdvantageScope.
- `RobotPoses`: A list of 3D poses representing the raw pose estimates from the last cycle. We recommend visualizing this field using the "Ghost" object on the 3D field tab in AdvantageScope.
- `RobotPosesAccepted`: A subset of the `RobotPoses` list with the set of estimates that passed all stages of filtering.
- `RobotPosesRejected`: A subset of the `RobotPoses` list with the set of estimates that were removed during filtering.

The `Summary` table includes identical fields which include samples from every camera.
