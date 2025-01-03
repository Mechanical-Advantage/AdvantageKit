# Installation

## New Projects

For new projects, we highly recommend starting with one of the [template projects](/category/template-projects) attached to the [latest release](https://github.com/Mechanical-Advantage/AdvantageKit/releases). These projects include detailed documentation and setup instructions for many common use cases:

- **[2025 KitBot Template](../template-projects/kitbot-2025-template.md)**: For robots based on the 2025 FIRST KitBot.
- **[Differential Drive Template](../template-projects/diff-drive-template.md)**: For other differential drive (tank) robots.
- **[Spark Swerve Template](../template-projects/spark-swerve-template.md)**: For swerve drives primarily using the Spark Max and Spark Flex, including NEO, NEO Vortex, or NEO 550 motors.
- **[TalonFX Swerve Template](../template-projects/talonfx-swerve-template.md)**: For swerve drives primarily using TalonFX-based motors like the Falcon 500, Kraken X60, and Kraken X44.
- **[Vision Template](../template-projects/vision-template.md)**: Example code for running simple vision targeting and pose estimation.
- **[Skeleton Template](../template-projects/skeleton-template.md)**: Simple project with AdvantageKit installed but without subsystems or control logic.

## Existing Projects

Users wishing to install AdvantageKit in an existing project should check the documentation page for [existing projects](./existing-projects.md).

## Offline Installation

Maven artifacts for AdvantageKit can be downloaded and installed for offline use. This allows AdvantageKit to be accessed even if GitHub Packages is blocked on school networks.

1. Download the "maven_offline.zip" asset attached to the latest [GitHub release](https://github.com/Mechanical-Advantage/AdvantageKit/releases/latest).
2. Unzip the file into "C:\Users\Public\wpilib\YEAR\maven" on Windows or "~/wpilib/YEAR/maven" on macOS/Linux.
