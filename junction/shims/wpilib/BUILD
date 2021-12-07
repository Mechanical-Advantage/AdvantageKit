load("@rules_jvm_external//:defs.bzl", "java_export")

java_library(
    name = "wpilib",
    srcs = glob(["src/**/*.java"]), # Having shims in the srcs will take priority over the duplicate copies in deps when using this target within bazel.
    deps = [
        "//third_party/allwpilib/wpilibj",
        "//junction/core"
    ],
)
java_export(
    name = "wpilib-export",
    maven_coordinates = "org.littletonrobotics.akit.junction:wpilib-shim:0.0.1",
    deploy_env = ["//junction/core"],

    runtime_deps = ["//third_party/allwpilib/wpilibj", ":wpilib"], # This needs to explicitly depend on wpilibj before our shim target because of how java_export handles duplicate class files when building.
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
