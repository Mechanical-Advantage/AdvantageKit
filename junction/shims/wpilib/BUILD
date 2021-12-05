load("@rules_jvm_external//:defs.bzl", "java_export")

java_library(
    name = "wpilib-shims",
    srcs = glob(["src/**/*.java"]),
    deps = [
        "//third_party/wpilib:wpilibj",
        "//junction/core:core-compile"
    ],
)

java_export(
    name = "wpilib",
    maven_coordinates = "org.littletonrobotics.akit.junction:wpilib-shim:0.0.1",

    runtime_deps = ["//third_party/wpilib:wpilibj",":wpilib-shims",  "//junction/core:core-compile"],
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
