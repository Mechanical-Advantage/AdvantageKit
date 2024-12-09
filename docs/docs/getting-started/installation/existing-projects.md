---
sidebar_position: 1
---

# Existing Projects

:::info
The instructions below are for the 2025 beta of AdvantageKit. To install the latest stable release (v3.2.1), following the instructions [here](https://github.com/Mechanical-Advantage/AdvantageKit/blob/v3.2.1/docs/INSTALLATION.md#existing-projects).
:::

To install the AdvantageKit vendordep, go to "WPILib: Manage Vendor Libraries" > "Install new libraries (online)" and paste in the URL below.

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

task(replayWatch, type: JavaExec) {
    mainClass = "org.littletonrobotics.junction.ReplayWatch"
    classpath = sourceSets.main.runtimeClasspath
}

dependencies {
    // ...
    def akitJson = new groovy.json.JsonSlurper().parseText(new File(projectDir.getAbsolutePath() + "/vendordeps/AdvantageKit.json").text)
    annotationProcessor "org.littletonrobotics.akit:akit-autolog:$akitJson.version"
}
```

AdvantageKit is available through GitHub Packages. Per [this issue](https://github.community/t/download-from-github-package-registry-without-authentication/14407), downloading packages from GitHub requires authentication, even for public repositories. The configuration above includes an access token so that anyone can download AdvantageKit. The obfuscation of the string hides it from GitHub's bot; **do not include the plain text token in any GitHub repository.** This will cause the token to be automatically revoked, requiring us to create and distribute a new token.

:::tip
A token for accessing GitHub packages and AdvantageKit can be created using any GitHub account. Under account settings, go to "Developer settings" > "Personal access token" > "Tokens (classic)" > "Generate new token" > "Generate new token (classic)" and enable the "read:packages" scope. Keep in mind that GitHub will revoke this token if the plaintext version appears in a GitHub repository.
:::

## Robot Configuration

The main `Robot` class **must inherit from `LoggedRobot`** (see below). `LoggedRobot` performs the same functions as `TimedRobot`, with some exceptions:

- It does not support adding extra periodic functions.
- The method `setUseTiming` allows the user code to disable periodic timing and run cycles as fast as possible during replay. The timestamp read by methods like `Timer.getFPGATimstamp()` will still match the timestamp from the real robot.

```java
public class Robot extends LoggedRobot {
    ...
}
```

The user program is responsible for configuring and initializing the logging framework. This setup should be placed in the constructor of `Robot` _before any other initialization_. An example configuration is provided below:

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

Logger.start(); // Start logging! No more data receivers, replay sources, or metadata values may be added.
```

:::info
By default, the `WPILOGWriter` class writes to a USB stick when running on the roboRIO. **A FAT32 formatted USB stick must be connected to one of the roboRIO USB ports**.
:::

This setup enters replay mode for all simulator runs. If you need to run the simulator without replay (e.g. a physics simulator or Romi), extra constants or selection logic is required. See the example projects for one method of implementing this logic.
