# Installation

## New Projects

For new projects, the easiest way to use AdvantageKit is to download one of the example projects attached to the [latest release](https://github.com/Mechanical-Advantage/AdvantageKit/releases/latest). After downloading and unzipping the file, just adjust the team number in ".wpilib/wpilib_preferences.json" and get started!

- **Skeleton Project:** Includes a basic `TimedRobot` style structure, which supports logging on a real robot, physics simulation, and replay in simulation. This template is ideal if you prefer to start from a minimal project.
- **2024 KitBot Project:** Example command based project for the [2024 FIRST KitBot](https://www.firstinspires.org/resource-library/frc/kitbot), including support for physics simulation, feedforward characterization, and PathPlanner. _Note that this project supports both brushed and brushless motors on the Spark Max, Spark Flex, Talon FX, and Talon SRX. A time-based auto is provided as an alternative to PathPlanner for robots without drive encoders._
- **Differential Drive Project:** Example command based project with a tank drive and flywheel, including support for physics simulation, feedforward characterization, and PathPlanner. This projects supports the Spark Max, Spark Flex, and Talon FX.
- **Swerve Drive Project:** Example command based project with a swerve drive and flywheel, including support for physics simulation, feedforward characterization, and PathPlanner. This projects supports the Spark Max, Spark Flex, and Talon FX.
- **Advanced Swerve Drive Project:** Identical to the "Swerve Drive Project" but with support for high-frequency odometry (e.g. 250Hz) on REV and CTRE hardware. This significantly increases complexity, but may improve the consistency of odometry measurements. More details can be found in the [announcement post](https://www.chiefdelphi.com/t/advantagekit-2024-log-replay-again/442968/54#advanced-swerve-drive-project-2).

To switch between modes, set the "mode" attribute in `Constants.java` to `REAL`, `SIM` (physics simulation), or `REPLAY`. To support the Spark Flex, replace all instances of `CANSparkMax` with `CANSparkFlex` in the Spark Max IO implementations (the changes to the API do not impact these projects).

:::info
Git must be installed to build the example projects. See [here](https://git-scm.com/downloads) for installation instructions.
:::

## Existing Projects

To install the AdvantageKit vendordep, go to "WPILib: Manage Vendor Libraries" > "Install new libraries (online)" and paste in the URL below. The changelog for the [latest release](https://github.com/Mechanical-Advantage/AdvantageKit/releases/latest) includes the WPILib version on which it is based; **you must use the same version in your robot project**. You can check the selected version of WPILib at the top of `build.gradle` after "edu.wpi.first.GradleRIO".

```
https://github.com/Mechanical-Advantage/AdvantageKit/releases/latest/download/AdvantageKit.json
```

Next, add the following blocks to the `build.gradle` file. Note that the lines under `dependencies` should be combined with the existing `dependencies` block.

```groovy
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/Mechanical-Advantage/AdvantageKit")
        credentials {
            username = "Mechanical-Advantage-Bot"
            password = "\u0067\u0068\u0070\u005f\u006e\u0056\u0051\u006a\u0055\u004f\u004c\u0061\u0079\u0066\u006e\u0078\u006e\u0037\u0051\u0049\u0054\u0042\u0032\u004c\u004a\u006d\u0055\u0070\u0073\u0031\u006d\u0037\u004c\u005a\u0030\u0076\u0062\u0070\u0063\u0051"
        }
    }
    mavenLocal()
}

dependencies {
    // ...
    def akitJson = new groovy.json.JsonSlurper().parseText(new File(projectDir.getAbsolutePath() + "/vendordeps/AdvantageKit.json").text)
    annotationProcessor "org.littletonrobotics.akit.junction:junction-autolog:$akitJson.version"
}
```

AdvantageKit is available through GitHub Packages. Per [this issue](https://github.community/t/download-from-github-package-registry-without-authentication/14407), downloading packages from GitHub requires authentication, even for public repositories. The configuration above includes an access token so that anyone can download AdvantageKit. The obfuscation of the string hides it from GitHub's bot; **do not include the plain text token in any GitHub repository.** This will cause the token to be automatically revoked, requiring us to create and distribute a new token.

:::tip
A token for accessing GitHub packages and AdvantageKit can be created using any GitHub account. Under account settings, go to "Developer settings" > "Personal access token" > "Tokens (classic)" > "Generate new token" > "Generate new token (classic)" and enable the "read:packages" scope. Keep in mind that GitHub will revoke this token if the plaintext version appears in a GitHub repository.
:::

## Offline Installation

Maven artifacts for AdvantageKit can be downloaded and installed for offline use. This allows AdvantageKit to be accessed even if GitHub Packages is blocked on school networks.

1. Download the "maven_offline.zip" asset attached to the latest [GitHub release](https://github.com/Mechanical-Advantage/AdvantageKit/releases/latest).
2. Unzip the file into "C:\Users\Public\wpilib\YEAR\maven" on Windows or "~/wpilib/YEAR/maven" on macOS/Linux.

## Robot Configuration

The main `Robot` class **must inherit from `LoggedRobot`** (see below). `LoggedRobot` performs the same functions as `TimedRobot`, with some exceptions:

- It does not support adding extra periodic functions.
- The method `setUseTiming` allows the user code to disable periodic timing and run cycles as fast as possible during replay. The timestamp read by methods like `Timer.getFPGATimstamp()` will still match the timestamp from the real robot.

```java
public class Robot extends LoggedRobot {
    ...
}
```

The user program is responsible for configuring and initializing the logging framework. This setup should be placed in `robotInit()` _before any other initialization_. An example configuration is provided below:

```java
Logger.recordMetadata("ProjectName", "MyProject"); // Set a metadata value

if (isReal()) {
    Logger.addDataReceiver(new WPILOGWriter()); // Log to a USB stick ("/U/logs")
    Logger.addDataReceiver(new NT4Publisher()); // Publish data to NetworkTables
    new PowerDistribution(1, ModuleType.kRev); // Enables power distribution logging
} else {
    setUseTiming(false); // Run as fast as possible
    String logPath = LogFileUtil.findReplayLog(); // Pull the replay log from AdvantageScope (or prompt the user)
    Logger.setReplaySource(new WPILOGReader(logPath)); // Read replay log
    Logger.addDataReceiver(new WPILOGWriter(LogFileUtil.addPathSuffix(logPath, "_sim"))); // Save outputs to a new log
}

// Logger.disableDeterministicTimestamps() // See "Deterministic Timestamps" in the "Understanding Data Flow" page
Logger.start(); // Start logging! No more data receivers, replay sources, or metadata values may be added.
```

:::info
By default, the `WPILOGWriter` class writes to a USB stick when running on the roboRIO. **A FAT32 formatted USB stick must be connected to one of the roboRIO USB ports**.
:::

This setup enters replay mode for all simulator runs. If you need to run the simulator without replay (e.g. a physics simulator or Romi), extra constants or selection logic is required. See the example projects for one method of implementing this logic.
