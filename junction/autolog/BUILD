load("//build_tools/repo:java_export.bzl", "java_export")

java_library(
    name = "autolog",
    srcs = glob(["src/**/*.java"]),
    resource_strip_prefix = "junction/autolog/src/resources",
    resources = ["src/resources/META-INF/services/javax.annotation.processing.Processor"],
    visibility = ["//visibility:public"],
    deps = [
        "//junction/core:core-export",
        "@maven//:com_squareup_javapoet",
    ],
)

java_export(
    name = "autolog-export",
    #deploy_env = ["//third_party/wpilib:wpilibj"],
    maven_coordinates = "org.littletonrobotics.akit.junction:junction-autolog:{publishing_version}",
    publish_tags = ["ci_publish"],
    visibility = ["//visibility:public"],
    runtime_deps = [":autolog"],
)
