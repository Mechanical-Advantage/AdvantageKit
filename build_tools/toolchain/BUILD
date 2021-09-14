toolchain(
    name = "athena-cc-toolchain-linux_x64",
    exec_compatible_with = ["@platforms//os:linux"],
    target_compatible_with = [
        "@platforms//os:linux",
        "@platforms//cpu:arm",
        "//build_tools/platforms:roborio",
    ],
    toolchain = "@athena_toolchain_linux_x64_files//:athena-cc-toolchain-def",
    toolchain_type = "@bazel_tools//tools/cpp:toolchain_type",
)

toolchain(
    name = "athena-cc-toolchain-windows_x64",
    exec_compatible_with = ["@platforms//os:windows"],
    target_compatible_with = [
        "@platforms//os:linux",
        "@platforms//cpu:arm",
        "//build_tools/platforms:roborio",
    ],
    toolchain = "@athena_toolchain_windows_x64_files//:athena-cc-toolchain-def",
    toolchain_type = "@bazel_tools//tools/cpp:toolchain_type",
)

toolchain(
    name = "athena-cc-toolchain-macos_x64",
    exec_compatible_with = ["@platforms//os:macos"],
    target_compatible_with = [
        "@platforms//os:linux",
        "@platforms//cpu:arm",
        "//build_tools/platforms:roborio",
    ],
    toolchain = "@athena_toolchain_macos_x64_files//:athena-cc-toolchain-def",
    toolchain_type = "@bazel_tools//tools/cpp:toolchain_type",
)
