load("@//build_tools/toolchain:athena_cc_toolchain.bzl", "athena_cc_toolchain_config")

filegroup(
    name = "toolchain-files",
    srcs = glob(["frc2022/roborio/**/*"]),
)

athena_cc_toolchain_config(
    name = "toolchain-config",
    toolchain_host = "macos_x64",
)

cc_toolchain(
    name = "athena-cc-toolchain-def",
    all_files = ":toolchain-files",
    ar_files = ":toolchain-files",
    compiler_files = ":toolchain-files",
    dwp_files = ":toolchain-files",
    linker_files = ":toolchain-files",
    objcopy_files = ":toolchain-files",
    strip_files = ":toolchain-files",
    toolchain_config = ":toolchain-config",
    visibility = ["//visibility:public"],
)
