load("@rules_java//java:defs.bzl", "java_library", "java_binary")
load("@rules_jvm_external//:defs.bzl", "java_export")

# This creates a Java library called "example" with a maven dependency
# Java libraries are meant to be used as targets internally within our own repository, and they don't provide
# any built in way to export them to a runnable jar or publish them to a maven repository.
java_library(
    name = "example",
    srcs = glob(["src/**/*.java"]),
    deps = ["@maven//:com_google_guava_guava"]
)

# This creates a "Java Export" rule, which can be used to generate a jar and pom file for publishing artifacts
# to a maven repository.  Additionally, it automatically creates a runnable rule which completes the publishing task.
# This rule can be run from GitHub Actions or another CI system to automatically build and publish Maven artifacts.

# Note that java_export rules have an underlying java_library rule with the same name, and also can take all
# arguments that java_library does.  So, we could simplify this further by combining the above library into one rule.
java_export(
    name = "exported_lib",
    maven_coordinates = "org.littletonrobotics.akit:example:0.0.1",
    runtime_deps = [":example"]
)

cc_binary(
    name = "native",
    srcs = ["test.c"],
    linkshared = True
)