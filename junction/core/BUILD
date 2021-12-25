load("//build_tools/repo:java_export.bzl", "java_export")

java_library(
    name = "core",
    srcs = glob(["src/**/*.java"]),
    tags = ["ci_build"],
    visibility = ["//visibility:public"],
    deps = [
        "//conduit/api:api-export",
        "//third_party/wpilib:wpilibj-compileonly",
    ],
)

java_library(
    name = "core-compile",
    neverlink = True,
    visibility = ["//visibility:public"],
    exports = [":core"],
)

java_export(
    name = "core-export",
    deploy_env = ["//third_party/wpilib:wpilibj"],
    maven_coordinates = "org.littletonrobotics.akit.junction:junction-core:{publishing_version}",
    publish_tags = ["ci_publish"],
    visibility = ["//visibility:public"],
    runtime_deps = [":core"],
)
