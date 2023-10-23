# Installing AdvantageKit

## New Projects

For new projects, the easiest way to use AdvantageKit is to download one of the example projects attached to the [latest release](https://github.com/Mechanical-Advantage/AdvantageKit/releases/latest). After downloading and unzipping the file, just adjust the team number in ".wpilib/wpilib_preferences.json" and get started!

- **Skeleton Project:** Includes a basic `TimedRobot` style structure, which supports logging on a real robot, physics simulation, and replay in simulation. This template is ideal if you prefer to start from a minimal project.
- **Swerve Drive Project:** Example command based project with a swerve drive and closed-loop flywheel, including support for physics simulation and replay. Additional features include odometry, field-oriented drive with joysticks, and auto paths with PathPlanner.
- **Differential Drive Project:** Example command based project with an open-loop tank drive and closed-loop flywheel, including support for physics simulation and replay.

> Note: To switch between modes, set the "mode" attribute in `Constants.java` to `REAL`, `SIM` (physics simulation), or `REPLAY`.

## Existing Projects

AdvantageKit is available through GitHub Packages. To access our Maven repository through Gradle, add the following block to the `build.gradle` file:

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

Per [this issue](https://github.community/t/download-from-github-package-registry-without-authentication/14407), downloading packages from GitHub requires authentication, even for public repositories. The configuration above includes an access token so that anyone can download AdvantageKit. The obfuscation of the string hides it from GitHub's bot; **do not include the plain text token in any GitHub repository.** This will cause the token to be automatically revoked, requiring us to create and distribute a new token.

> Note: A token for accessing GitHub packages and AdvantageKit can be created using any GitHub account. Under account settings, go to "Developer settings" > "Personal access token" > "Tokens (classic)" > "Generate new token" > "Generate new token (classic)" and enable the "read:packages" scope. Keep in mind that GitHub will revoke this token if the plaintext version appears in a GitHub repository.

AdvantageKit uses WPILib shims to inject data. Add the following block to `build.gradle` to replace the default implementation. **This is required for the framework to function**

```groovy
configurations.all {
    exclude group: "edu.wpi.first.wpilibj"
}
```

To install the main AdvantageKit packages, go to "WPILib: Manage Vendor Libraries" > "Install new libraries (online)" and paste in the URL below. The changelog for the [latest release](https://github.com/Mechanical-Advantage/AdvantageKit/releases/latest) includes the WPILib version on which it is based; **you must use the same version in your robot project**. You can check the selected version of WPILib at the top of `build.gradle` after "edu.wpi.first.GradleRIO".

```
https://github.com/Mechanical-Advantage/AdvantageKit/releases/latest/download/AdvantageKit.json
```

### Robot Configuration

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
    Logger.addDataReceiver(new WPILOGWriter("/U")); // Log to a USB stick
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

This setup enters replay mode for all simulator runs. If you need to run the simulator without replay (e.g. a physics simulator or Romi), extra constants or selection logic is required. See the example projects for one method of implementing this logic.

### `@AutoLog` Annotation

The [`@AutoLog` annotation](CODE-STRUCTURE.md#autolog-annotation) automatically generates classes for input logging from subsystems. To install `@AutoLog`, modify your `build.gradle` to include:

```groovy
dependencies {
    // ...
    def akitJson = new groovy.json.JsonSlurper().parseText(new File(projectDir.getAbsolutePath() + "/vendordeps/AdvantageKit.json").text)
    annotationProcessor "org.littletonrobotics.akit.junction:junction-autolog:$akitJson.version"
}
```

### Gversion Plugin (Git Metadata)

We recommend using the [gversion](https://github.com/lessthanoptimal/gversion-plugin) Gradle plugin to record metadata like the git hash and build date. To install it, add the plugin at the top of `build.gradle`:

```groovy
plugins {
    // ...
    id "com.peterabeles.gversion" version "1.10"
}
```

Add the `createVersionFile` task as a dependency of `compileJava`:

```groovy
project.compileJava.dependsOn(createVersionFile)
gversion {
  srcDir       = "src/main/java/"
  classPackage = "frc.robot"
  className    = "BuildConstants"
  dateFormat   = "yyyy-MM-dd HH:mm:ss z"
  timeZone     = "America/New_York" // Use preferred time zone
  indent       = "  "
}
```

You should also add the `BuildConstants.java` file to the repository `.gitignore`:

```
src/main/java/frc/robot/BuildConstants.java
```
