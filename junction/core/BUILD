load("@rules_jvm_external//:defs.bzl", "java_export")

java_library(
    name = "core",
    srcs = glob(["src/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//conduit/api",
        "//third_party/wpilib:wpilibj-compileonly",
    ],
)
java_library(
    name = "core-compile",
    exports = [":core"],
    neverlink = True,
    visibility = ["//visibility:public"],
)

java_export(
    name = "core-export",
    srcs = glob(["src/**/*.java"]),
    maven_coordinates = "org.littletonrobotics.akit.junction:junction-core:0.0.1",
    deps = [
        "//conduit/api",
        "//third_party/wpilib:wpilibj-compileonly",
    ],
    visibility = ["//visibility:public"]
)