load("//build_tools/repo:java_export.bzl", "java_export")

java_library(
    name = "wpilib",
    srcs = glob(["src/**/*.java"]),  # Having shims in the srcs will take priority over the duplicate copies in deps when using this target within bazel.
    tags = ["ci_build"],
    visibility = ["//visibility:public"],
    exports = ["//third_party/wpilib:wpilibj"],
    deps = [
        "//junction/core",
        "//third_party/wpilib:wpilibj",
    ],
)

java_export(
    name = "wpilib-export",
    deploy_env = ["//junction/core"],  # Don't include //junction/core classes in the output jar
    maven_coordinates = "org.littletonrobotics.akit.junction:wpilib-shim:{publishing_version}",
    publish_tags = ["ci_publish"],
    # Prevent the formatter from re-ordering the runtime_deps list, as this would break the shim output
    # buildifier: leave-alone
    runtime_deps = [
        "//third_party/wpilib:wpilibj",
        ":wpilib",
    ],  # This needs to explicitly depend on wpilibj before our shim target because of how java_export handles duplicate class files when building.
)

java_test(
    name = "jtest",
    args = [
        "$(location :wpilib)",
        "$(location @maven//:edu_wpi_first_wpilibj_wpilibj_java)",
    ],
    data = [
        ":wpilib",
        "@maven//:edu_wpi_first_wpilibj_wpilibj_java",
    ],
    main_class = "Sigcheck",
    use_testrunner = False,
    runtime_deps = [
        ":wpilib",
        "//build_tools/jpatch:sigcheck",
        "@maven//:edu_wpi_first_wpilibj_wpilibj_java",
    ],
)
