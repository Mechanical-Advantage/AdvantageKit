load("//build_tools/repo:java_export.bzl", "java_export")
load("@contrib_rules_jvm//java:defs.bzl", "java_test_suite")

java_library(
    name = "core",
    srcs = glob(["src/main/**/*.java"]),
    tags = ["ci_build"],
    visibility = ["//visibility:public"],
    deps = [
        "//conduit/api:api-export",
        "//third_party/wpilib:wpilibj-compileonly",
        "@maven//:us_hebi_quickbuf_quickbuf_runtime",
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

java_test_suite(
    name = "core-tests",
    srcs = glob(["src/test/**/*.java"]),
    runner = "junit5",
    runtime_deps = [
        "@maven//:org_junit_jupiter_junit_jupiter_engine",
        "@maven//:org_junit_platform_junit_platform_launcher",
        "@maven//:org_junit_platform_junit_platform_reporting",
    ],
    deps = [
        ":core-export",
        "@maven//:org_junit_jupiter_junit_jupiter_api",
    ],
)
