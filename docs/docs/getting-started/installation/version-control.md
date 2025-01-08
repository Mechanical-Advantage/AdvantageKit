---
sidebar_position: 2
---

# Version Control

Typically, log replay requires that the code running on the robot and in the simulator are identical. We recommend the following tools to achieve that, enabled by version control with Git.

:::tip
These tools are already installed in the AdvantageKit [template projects](/category/template-projects), but this page explains their intended usage in more detail.
:::

## Gversion

The [gversion](https://github.com/lessthanoptimal/gversion-plugin) Gradle plugin produces a constants file with important metadata, including the [Git hash](https://www.mikestreety.co.uk/blog/the-git-commit-hash/) uniquely identifying each commit. It also includes whether the tree is "dirty" (if it included uncommitted changes). The template projects include Gversion already. Otherwise, follow the installation instructions below.

<details>
<summary>Installation</summary>

Add the plugin at the top of `build.gradle`:

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

:::info
Git must be installed and available on the PATH to use the Gversion plugin. See [here](https://git-scm.com/downloads).
:::

</details>

Metadata can be recorded in the log file as shown below:

```java
public Robot() {
    Logger.recordMetadata("GitSHA", BuildConstants.GIT_SHA);
    // ...

    Logger.start();
}
```

The metadata values can be viewed using AdvantageScope's 🔍 [Metadata](https://docs.advantagescope.org/tab-reference/metadata) tab. Running `git checkout ??????...` with the commit hash will return to the same version of code that was running on the robot (except for any uncommitted changes).

## Event Deploy

Code often changes repeatedly during competition, which would normally mean running code with uncommitted change. This is a problem for log replay, since the version of code running in a particular match may be impossible to recreate afterwards.

This can be addressed by including a Gradle task to automatically commit working changes to a temporary branch before every deploy.

### Installation

The Gradle task is preconfigured in the AdvantageKit template projects. Add the following lines to `build.gradle`:

```groovy
// Create commit with working changes on event branches
task(eventDeploy) {
    doLast {
        if (project.gradle.startParameter.taskNames.any({ it.toLowerCase().contains("deploy") })) {
            def branchPrefix = "event"
            def branch = 'git branch --show-current'.execute().text.trim()
            def commitMessage = "Update at '${new Date().toString()}'"

            if (branch.startsWith(branchPrefix)) {
                exec {
                    workingDir(projectDir)
                    executable 'git'
                    args 'add', '-A'
                }
                exec {
                    workingDir(projectDir)
                    executable 'git'
                    args 'commit', '-m', commitMessage
                    ignoreExitValue = true
                }

                println "Committed to branch: '$branch'"
                println "Commit message: '$commitMessage'"
            } else {
                println "Not on an event branch, skipping commit"
            }
        } else {
            println "Not running deploy task, skipping commit"
        }
    }
}
createVersionFile.dependsOn(eventDeploy)
```

### Usage

1. Before the event, create and check out a branch that starts with "event" such as "event_nhgrs". We recommend creating a new branch for each event.
2. Deploy robot code through any method supported by WPILib.
3. A commit is automatically created with all changes since the last commit before the deploy begins. The name of the commit includes the current timestamp (e.g. "Update at "1/31/2022, 8:30:00 AM").
4. At the end of the event, the branch can be ["squashed and merged"](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/incorporating-changes-from-a-pull-request/about-pull-request-merges#squash-and-merge-your-commits) back to a normal development branch, keeping the Git history clean.
5. When running log replay, find the commit hash in the log file metadata and run `git checkout` as described in the previous section. This will return to the exact version of code running on the robot (even if the commits were later squashed and merged). Since all of the changes were committed before each deploy, the simulated code is guaranteed to be identical to the original robot code.
