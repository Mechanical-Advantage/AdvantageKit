"""
Lists of maven artifact strings that AdvantageKit uses.  They are defined here for two reasons:
    
    1. If another project wished to depend on AdvantageKit, this is the recommended way to share maven_install
       dependencies with the consuming project.
    2. We use this list to extract the list of genrules which copy the downloaded files (since we use pinning)
       from the build root (bazel-AdvantageKit) to the genfiles directory (bazel-out and bazel-bin).
       We need to do this so VSCode can see all of the jar files in one place, and so that they don't get removed
       from view when we run a build on a target that doesn't use them (Bazel clears the bazel-AdvantageKit directory
       on every build).  See the BUILD file at the root of the workspace for more info on this.
"""

MAVEN_ARTIFACTS = [
    "io.github.classgraph:classgraph:4.8.128",
    "com.fasterxml.jackson.core:jackson-annotations:2.10.0",
    "com.fasterxml.jackson.core:jackson-core:2.10.0",
    "com.fasterxml.jackson.core:jackson-databind:2.10.0",
    "org.ejml:ejml-simple:0.38",
    "org.ejml:ejml-core:0.38",
    "junit:junit:4.13.2"
]

FRCMAVEN_ARTIFACTS = [
    "edu.wpi.first.hal:hal-java:2021.3.1",
    "edu.wpi.first.wpiutil:wpiutil-java:2021.3.1",
    "edu.wpi.first.wpimath:wpimath-java:2021.3.1",
    "edu.wpi.first.ntcore:ntcore-java:2021.3.1",
    "edu.wpi.first.thirdparty.frc2021.opencv:opencv-java:3.4.7-5",
    "edu.wpi.first.cscore:cscore-java:2021.3.1",
    "edu.wpi.first.cameraserver:cameraserver-java:2021.3.1",
]