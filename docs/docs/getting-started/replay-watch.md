---
sidebar_position: 5
---

# How To: Replay Watch

Some use cases of log replay benefit from rapid iteration, such as tuning pose estimation algorithms. **Replay watch** addresses this use case by automatically updating replayed outputs when the code is modified. An example is shown below, where the replayed output in AdvantageScope updates in real-time as the code is modified:

<iframe width="100%" style={{"aspect-ratio": "16 / 9"}} src="https://www.youtube.com/embed/TYRNqW8SrkE" title="AdvantageKit Replay Watch Demo (Simple)" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" referrerpolicy="strict-origin-when-cross-origin" allowfullscreen></iframe>

:::info
Check the replay example on [rapid iteration](./what-is-advantagekit/example-rapid-iteration.md) for a more detailed example of this feature in the context of tuning a pose estimation algorithm.
:::

## Usage

Replay watch requires a custom Gradle task defined in `build.gradle`. This task is included in the AdvantageKit template projects and documented in the installation instructions for [existing projects](./installation/existing-projects.md).

1. Configure the project for log replay as normal, such as setting `simMode` in `Constants.java` to `REPLAY` in the AdvantageKit template projects. The `WPILOGWriter` used during replay should be configured using the default, `AUTO`, or `ALWAYS` AdvantageScope open behavior (check the API docs for details). We also recommend calling `setUseTiming(false)` during setup, as described [here](./traditional-replay.md#setup).

2. Open the original log file in AdvantageScope or add the path to the `AKIT_LOG_PATH` environment variable.

3. Run `./gradlew replayWatch` (macOS/Linux) or `gradle.bat replayWatch` (Windows) from the command line.

4. Log replay will run once, and the resulting log file will open automatically in AdvantageScope.

5. When the contents of the `src` directory are modified, log replay will run again automatically and the new log will be opened in AdvantageScope. The time range and visualizations in AdvantageScope will be preserved when loading new data. Note that each iteration of log replay will _overwrite_ the previous replayed log file, but will not modify the original log.

:::tip
Replay watch is limited by the speed at which the robot program can be replayed, which makes it most useful on **short log files** and devices with **fast single-core CPU performance**.
:::
