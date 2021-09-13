load("@bazel_tools//tools/cpp:cc_toolchain_config_lib.bzl", "feature", "flag_group", "flag_set", "tool_path", "with_feature_set")
load("@bazel_tools//tools/build_defs/cc:action_names.bzl", "ACTION_NAMES")

def _impl(ctx):
    tool_paths = [
        tool_path(
            name = "gcc",
            path = "frc2021/roborio/bin/arm-frc2021-linux-gnueabi-gcc",
        ),
        tool_path(
            name = "ld",
            path = "frc2021/roborio/bin/arm-frc2021-linux-gnueabi-ld",
        ),
        tool_path(
            name = "ar",
            path = "frc2021/roborio/bin/arm-frc2021-linux-gnueabi-ar",
        ),
        tool_path(
            name = "cpp",
            path = "frc2021/roborio/bin/arm-frc2021-linux-gnueabi-cpp",
        ),
        tool_path(
            name = "gcov",
            path = "frc2021/roborio/bin/arm-frc2021-linux-gnueabi-gcov",
        ),
        tool_path(
            name = "nm",
            path = "frc2021/roborio/bin/arm-frc2021-linux-gnueabi-nm",
        ),
        tool_path(
            name = "objdump",
            path = "frc2021/roborio/bin/arm-frc2021-linux-gnueabi-objdump",
        ),
        tool_path(
            name = "strip",
            path = "frc2021/roborio/bin/arm-frc2021-linux-gnueabi-strip",
        ),
    ]
    features = [
        feature(name = "dbg"),  # For some reason we have to redefine dbg and opt modes here?? not really sure why
        feature(name = "opt"),
        feature(
            name = "athena_default_compiler_flags",
            enabled = True,
            flag_sets = [
                flag_set(
                    actions = [ACTION_NAMES.c_compile, ACTION_NAMES.cpp_compile, ACTION_NAMES.assemble, ACTION_NAMES.preprocess_assemble, ACTION_NAMES.linkstamp_compile, ACTION_NAMES.cpp_header_parsing],
                    flag_groups = [
                        flag_group(
                            flags = ["-O0", "-ggdb", "-gdwarf-2", "-g3"],
                        ),
                    ],
                    with_features = [with_feature_set(features = ["dbg"])],
                ),
                flag_set(
                    actions = [ACTION_NAMES.c_compile, ACTION_NAMES.cpp_compile],
                    flag_groups = [
                        flag_group(
                            flags = ["-Os"],
                        ),
                    ],
                    with_features = [with_feature_set(features = ["opt"])],
                ),
                flag_set(
                    actions = [ACTION_NAMES.c_compile, ACTION_NAMES.cpp_compile],
                    flag_groups = [
                        flag_group(
                            flags = ["-no-canonical-prefixes", "-fno-canonical-system-headers"],
                        ),
                    ],
                ),
            ],
        ),
    ]

    return cc_common.create_cc_toolchain_config_info(
        ctx = ctx,
        builtin_sysroot = "external/athena_toolchain_%s_files/frc2021/roborio/arm-frc2021-linux-gnueabi" % ctx.attr.toolchain_host,
        toolchain_identifier = "athena_cc_toolchain_%s" % ctx.attr.toolchain_host,
        host_system_name = "local",
        target_system_name = "linux-gnueabi",
        target_cpu = "arm",
        target_libc = "glibc-2.24",
        compiler = "arm-frc2021-linux-gnueabi-gcc-7.3.0",
        abi_version = "unknown",
        abi_libc_version = "unknown",
        tool_paths = tool_paths,
        features = features,
    )

athena_cc_toolchain_config = rule(
    implementation = _impl,
    attrs = {
        "toolchain_host": attr.string()
    },
    provides = [CcToolchainConfigInfo],
)