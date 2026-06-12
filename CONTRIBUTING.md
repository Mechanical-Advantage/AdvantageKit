# Contributing to AdvantageKit

Thank you for your interest in contributing to **AdvantageKit**! This project is maintained by **Littleton Robotics** and the community. To ensure that AdvantageKit remains reliable for all teams, we have established a set of guidelines for contributions.

Please remember to behave with **Gracious Professionalism** in all interactions. Any questions or concerns can be directed to software@team6328.org.

## General Contribution Rules

- **Reliability is Paramount:** AdvantageKit is infrastructure. Changes must not break existing logging capabilities or introduce non-determinism into the replay cycle.
- **Backward Compatibility:** Where possible, breaking changes should follow a one year deprecation cycle to minimize friction for users.
- **Documentation:** Most code changes require corresponding updates to the documentation. Please ask us for help if you're not sure where to start.
- **Broad Applicability:** Features should be useful to a wide range of teams. New logging formats should be broadly supported and useful to many users.

## What to Contribute

- **Bug Reports & Fixes:** We welcome fixes for bugs found during or after the season. Please submit a GitHub issue first to track the bug.
- **Feature Improvements:** Improvements to the AdvantageKit API are welcome, but feature changes will generally not be merged until _after_ the competition season. For large changes, please open a GitHub issue or contact us at software@team6328.org before starting work.

## Development Setup

1. **Java Version:** Ensure you have JDK 17 or newer installed.
2. **Clone the Repository:**

```bash
git clone https://github.com/Mechanical-Advantage/AdvantageKit.git
cd AdvantageKit
```

3. **Install the roboRIO Toolchain:** (if needed)

```bash
./gradlew :akit:installRoboRioToolchain
```

4. **Build the Project:**

```bash
./gradlew :akit:build
```

## Building and Testing Changes

Testing your changes usually requires building a local copy of the library and consuming it in a separate robot project (e.g., a starter project or your team's code).

### Local Publishing Workflow

To test your changes, you must publish the library to your local Maven repository. You can use the `publishToMavenLocal` Gradle task for this.

1. **Make your changes** in the AdvantageKit repository.
2. **Publish to Maven Local:**
   Run the following command in the root of the AdvantageKit repo:

```bash
./gradlew :akit:publishToMavenLocal
```

This compiles the code and places the artifacts (JARs and native zips) into your local user's `.m2` directory (e.g., `~/.m2/repository/`).

### Testing in a Robot Project

To use your locally built version of AdvantageKit in a robot project:

1. Open your robot project.
2. Locate the `vendordeps/AdvantageKit.json` file.
3. **Update the Version:** Change the version field to `"dev"`.

```json
{
  "fileName": "AdvantageKit.json",
  "name": "AdvantageKit",
  "version": "dev",
  "uuid": "...",
  ...
}
```

4. **Add the Maven Local repository:** Add the local repository to the `build.gradle` file.

```groovy
repositories {
    mavenLocal()
}
```

5. **Test:** Deploy your robot code or run it in simulation. It will now be using the modified AdvantageKit library from your machine.

**Important:** Do not commit the `"version": "dev"` change to your robot project's version control if you intend to share it with others who do not have your local build.

## Building Template Projects

Template projects are stored in the `template_projects` folder and can be built using a Bash script:

```bash
cd template_projects
./generate_projects.sh
```

Generated projects are stored in the `template_projects/generated` folder and can be opened separately in VSCode for editing with full IntelliSense. Note that the `./generate_projects.sh` script will _overwrite_ any changes in the generated projects. Copy any changes to the `template_projects/sources` folder first.

You can also run the following script to verify that all generated projects can build successfully:

```bash
cd template_projects
./build_generated_projects.sh
```

## Developing Documentation

Documentation is stored in the `docs` folder and is built using [Docusaurus](https://docusaurus.io).

1. **Node.js Installation:** Ensure that Node.js 20 or later is installed.
2. **Change Folders:** Navigate to the `docs` folder.
3. **Install Dependencies:**

```bash
npm install
```

4. **Launch Development Server:**

```bash
npm start
```

5. **Build the Full Documentation:**

```bash
npm run build
```

## Coding Guidelines & Formatting

AdvantageKit enforces code formatting using **Spotless**.

### Checking Formatting

To check if your code meets the style guidelines without modifying files:

```bash
./gradlew spotlessCheck
```

### Applying Formatting

To automatically fix formatting issues:

```bash
./gradlew spotlessApply
```

It is highly recommended to run `./gradlew spotlessApply` before every commit to ensure your PR checks pass.

## Submitting Changes

### Pull Request Process

1. **Fork** the repository and push your changes to a branch on your fork.
2. **Open a Pull Request (PR)** against the `main` branch of AdvantageKit.
3. **Description:** Clearly explain _what_ you changed, _why_ you changed it, and _how_ you tested it.
4. **Documentation:** If your change adds a new feature or alters behavior, please indicate if documentation updates are needed.

### Review

Your code will be reviewed by maintainers, and we may request changes to ensure code style consistency, API stability, or performance. AdvantageKit is developed by volunteers, so code reviews may be delayed (especially during the competition season). Please reach out to us via GitHub or email (software@team6328.org) if you have any questions or concerns about an open pull request.

## Licensing

By contributing to AdvantageKit, you agree that your code will be distributed under the project's BSD license ([link](LICENSE)). You should not contribute code that you do not have permission to relicense in this manner, such as code that is licensed under GPL that you do not have permission to relicense.
