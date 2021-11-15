toolchain_type(name = "toolchain_type")

toolchain(
    name = "flatc_toolchain_linux_x64",
    exec_compatible_with = ["@platforms//os:linux"],
    toolchain = "@flatc_toolchain_linux_x64_files//:flatc_toolchain_def",
    toolchain_type = ":toolchain_type",
)

toolchain(
    name = "flatc_toolchain_windows_x64",
    exec_compatible_with = ["@platforms//os:windows"],
    toolchain = "@flatc_toolchain_windows_x64_files//:flatc_toolchain_def",
    toolchain_type = ":toolchain_type",
)

toolchain(
    name = "flatc_toolchain_macos_x64",
    exec_compatible_with = ["@platforms//os:macos"],
    toolchain = "@flatc_toolchain_macos_x64_files//:flatc_toolchain_def",
    toolchain_type = ":toolchain_type",
)