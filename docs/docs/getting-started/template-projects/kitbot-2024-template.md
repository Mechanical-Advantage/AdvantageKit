---
sidebar_position: 1
---

# 2024 KitBot Template

The 202 KitBot template is designed for robots based on the design of the 2024 [FIRST KitBot](https://www.firstinspires.org/resource-library/frc/kitbot). It includes all of the features of the [differential drive template](./diff-drive-template.md), along with a built-in launcher subsystem and simple autonomous routine. It supports a wide variety of hardware, including Spark Max/Flex, Talon SRX, and TalonFX controllers along with the navX, Pigeon 2, and similar gyros.

:::info
The AdvantageKit 2024 KitBot template is **open-source** and **fully customizable**:

- **No black boxes:** Users can view and adjust all layers of the drive and launcher control stack.
- **Customizable:** IO implementations can be adjusted to support any hardware configuration (see the [customization](./diff-drive-template.md#customization) section).
- **Replayable:** Every aspect of the drive control logic, launcher control, etc. can be replayed and logged in simulation using AdvantageKit's deterministic replay features with _guaranteed accuracy_.

:::

## Setup

:::warning
This example project is part of the 2025 AdvantageKit beta release. If you encounter any issues during setup, please [open an issue](https://github.com/Mechanical-Advantage/AdvantageKit/issues).
:::

1. Download the 2024 KitBot template project from the AdvantageKit release on GitHub and open it in VSCode.

2. Set up the drive subsystem using the instructions found [here](./diff-drive-template.md#setup).

3. In the constructor of `RobotContainer`, switch the IO implementations instantiated for the launcher based on your chosen hardware. The default is the Talon SRX.

4. In the selected launcher IO implementation, update the device CAN IDs to the correct CAN IDs of the motor controllers (as configured in Phoenix Tuner or REV Hardware Client)
