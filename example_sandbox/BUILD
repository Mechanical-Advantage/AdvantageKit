load("@rules_java//java:defs.bzl", "java_library")
load("@rules_jvm_external//:defs.bzl", "java_export")

java_binary(
    name = "example",
    srcs = glob(["src/**/*.java"]),
    deps = ["//junction/shims/wpilib", "//third_party/wpilib:halsim_gui", "//third_party/wpilib:wpiHaljni"],
    env = {"HALSIM_EXTENSIONS": "$(location //third_party/wpilib:halsim_gui)"},
    main_class = "org.littletonrobotics.example.Example",
)
