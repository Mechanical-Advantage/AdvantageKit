# Version Control

Typically, log replay requires that the code running on the robot and in the simulator are identical. We recommend the following tools to achieve that, enabled by version control with Git.

## Gversion

The [gversion](https://github.com/lessthanoptimal/gversion-plugin) Gradle plugin produces a constants file with important metadata, including the [Git hash](https://www.mikestreety.co.uk/blog/the-git-commit-hash/) uniquely identifying each commit. It also includes whether the tree is "dirty" (if it included uncommitted changes).

The example projects include Gversion already, and the installation page shows [how to install the plugin](INSTALLATION.md#gversion-plugin-git-metadata) in an existing project. Metadata can be recorded in the log file as shown below:

```java
public void robotInit() {
    Logger.recordMetadata("GitSHA", BuildConstants.GIT_SHA);
    // ...

    Logger.start();
}
```

The metadata values can be viewed using AdvantageScope's 🔍 [Metadata](https://github.com/Mechanical-Advantage/AdvantageScope/blob/main/docs/tabs/METADATA.md) tab. Running `git checkout ??????...` with the commit hash will return to the same version of code that was running on the robot (except for any uncommitted changes).

## Event Deploy

Code often changes repeatedly during competition, which would normally mean running code with uncommitted change. This is a problem for log replay, since the version of code running in a particular match may be impossible to recreate afterwards. We have developed a VSCode extension to help with this issue by creating automatic commits to a temporary branch before every deploy.

### Installation

To install the extension, search for "Event Deploy for WPILib" in the VSCode extensions window and click "Install". Alternatively, it can be installed it from the [online marketplace](https://marketplace.visualstudio.com/items?itemName=Mechanical-Advantage.event-deploy-wpilib) or by cloning the [GitHub repository](https://github.com/Mechanical-Advantage/EventDeployExtension) (instructions in the README).

### Usage

1. Before the event, create and check out a branch that starts with "event" such as "event_nhgrs". We recommend creating a new branch for each event.
2. When deploying, click "Deploy Robot Code (Event)" in the editor menu. This options appears directly under the normal "Deploy Robot Code" option from WPILib.
3. A commit is automatically created with all changes since the last commit, and a deploy is started normally. The name of the commit includes the current timestamp (e.g. "Update at "1/31/2022, 8:30:00 AM").
4. At the end of the event, the branch can be ["squashed and merged"](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/incorporating-changes-from-a-pull-request/about-pull-request-merges#squash-and-merge-your-commits) back to a normal development branch, keeping the Git history clean.
5. When running log replay, find the commit hash in the log file metadata and run `git checkout` as described in the previous section. This will return to the exact version of code running on the robot (even if the commits were later squashed and merged). Since all of the changes were committed before each deploy, the simulated code is guaranteed to be identical to the original robot code.
