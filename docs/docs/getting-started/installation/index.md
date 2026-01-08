# 📦 Installation

:::tip
Looking to install AdvantageKit in a [Python robot project](https://docs.wpilib.org/en/stable/docs/software/python/index.html)? Consider using **[PyKit](https://github.com/1757WestwoodRobotics/PyKit)**, an alternative to AdvantageKit developed by [Team 1757](https://whsrobotics.org) that supports deterministic replay in Python.
:::

## New Projects

:::info
Template projects are not currently available for the 2027 alpha versions of AdvantageKit.
:::

For new projects, we highly recommend starting with one of the [template projects](/getting-started/template-projects) attached to the [latest release](https://github.com/Mechanical-Advantage/AdvantageKit/releases). These projects include detailed documentation and setup instructions for many common use cases:

- **[2026 KitBot Template](../template-projects/kitbot-template.md)**: For robots based on the 2026 FIRST KitBot.
- **[Differential Drive Template](../template-projects/diff-drive-template.md)**: For other differential drive (tank) robots.
- **[Spark Swerve Template](../template-projects/spark-swerve-template.md)**: For swerve drives primarily using the Spark Max and Spark Flex, including NEO, NEO Vortex, or NEO 550 motors.
- **[TalonFX(S) Swerve Template](../template-projects/talonfx-swerve-template.md)**: For swerve drives primarily using TalonFX(S)-based motors like the Falcon 500, Kraken X60, Kraken X44, and Minion.
- **[Vision Template](../template-projects/vision-template.md)**: Example code for running simple vision targeting and pose estimation.
- **[Skeleton Template](../template-projects/skeleton-template.md)**: Simple project with AdvantageKit installed but without subsystems or control logic.

## Existing Projects

Users wishing to install AdvantageKit in an existing project should check the documentation page for [existing projects](./existing-projects.md).

## Offline Installation

Maven artifacts for AdvantageKit can be downloaded and installed for offline use. This allows AdvantageKit to be accessed even if the Maven repository is blocked on school networks.

1. Download the "maven_offline.zip" asset attached to the latest [GitHub release](https://github.com/Mechanical-Advantage/AdvantageKit/releases/latest).
2. Unzip the file into "C:\Users\Public\wpilib\YEAR\maven" on Windows or "~/wpilib/YEAR/maven" on macOS/Linux.

## Legacy Projects

Projects based on AdvantageKit v4.0.0-beta-1 or earlier may experience build failures due to the use of an invalid GitHub Packages token. To address this issue, these releases have been republished to the current Maven repository used by v4.0.0 and later (which does not require authentication). Please follow the steps below to switch to the new Maven repository:

1. Ensure that you are using the _original_ vendordep JSON file (all versions of the vendordep JSON can be found on the [GitHub releases page](https://github.com/Mechanical-Advantage/AdvantageKit/releases)). **Do not modify this JSON file.**

2. Find the block below in `build.gradle` which configured the GitHub Packages repository:

```groovy
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/Mechanical-Advantage/AdvantageKit")
        credentials {
            username = "Mechanical-Advantage-Bot"
            password = "\u0067\u0068\u0070\u005f\u006e\u0056\u0051\u006a\u0055\u004f\u004c\u0061\u0079\u0066\u006e\u0078\u006e\u0037\u0051\u0049\u0054\u0042\u0032\u004c\u004a\u006d\u0055\u0070\u0073\u0031\u006d\u0037\u004c\u005a\u0030\u0076\u0062\u0070\u0063\u0051"
        }
    }
}
```

3. Replace that block with the new version shown below:

```groovy
repositories {
    maven {
        url = uri("https://frcmaven.wpi.edu/artifactory/littletonrobotics-mvn-release")
    }
}
```
