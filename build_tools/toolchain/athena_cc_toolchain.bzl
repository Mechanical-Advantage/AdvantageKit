load("@bazel_tools//tools/cpp:cc_toolchain_config_lib.bzl", "feature", "flag_group", "flag_set", "tool_path")
load("@bazel_tools//tools/build_defs/cc:action_names.bzl", "ACTION_NAMES")

all_compile_actions = [
    ACTION_NAMES.c_compile,
    ACTION_NAMES.cpp_compile,
    ACTION_NAMES.linkstamp_compile,
    ACTION_NAMES.assemble,
    ACTION_NAMES.preprocess_assemble,
    ACTION_NAMES.cpp_header_parsing,
    ACTION_NAMES.cpp_module_compile,
    ACTION_NAMES.cpp_module_codegen,
    ACTION_NAMES.clif_match,
    ACTION_NAMES.lto_backend,
]

all_cpp_compile_actions = [
    ACTION_NAMES.cpp_compile,
    ACTION_NAMES.linkstamp_compile,
    ACTION_NAMES.cpp_header_parsing,
    ACTION_NAMES.cpp_module_compile,
    ACTION_NAMES.cpp_module_codegen,
    ACTION_NAMES.clif_match,
]

preprocessor_compile_actions = [
    ACTION_NAMES.c_compile,
    ACTION_NAMES.cpp_compile,
    ACTION_NAMES.linkstamp_compile,
    ACTION_NAMES.preprocess_assemble,
    ACTION_NAMES.cpp_header_parsing,
    ACTION_NAMES.cpp_module_compile,
    ACTION_NAMES.clif_match,
]

codegen_compile_actions = [
    ACTION_NAMES.c_compile,
    ACTION_NAMES.cpp_compile,
    ACTION_NAMES.linkstamp_compile,
    ACTION_NAMES.assemble,
    ACTION_NAMES.preprocess_assemble,
    ACTION_NAMES.cpp_module_codegen,
    ACTION_NAMES.lto_backend,
]

all_link_actions = [
    ACTION_NAMES.cpp_link_executable,
    ACTION_NAMES.cpp_link_dynamic_library,
    ACTION_NAMES.cpp_link_nodeps_dynamic_library,
]

lto_index_actions = [
    ACTION_NAMES.lto_index_for_executable,
    ACTION_NAMES.lto_index_for_dynamic_library,
    ACTION_NAMES.lto_index_for_nodeps_dynamic_library,
]

def _impl(ctx):
    exec_extension = ".exe" if "windows" in ctx.attr.toolchain_host else ""

    tool_paths = [
        tool_path(
            name = name,
            path = "frc2022/roborio/bin/arm-frc2022-linux-gnueabi-{}{}".format(name, exec_extension),
        )
        for name in [
            "ar",
            "cpp",
            "gcc",
            "gcov",
            "ld",
            "nm",
            "objdump",
            "strip",
        ]
    ]

    features = [
        feature(name = "supports_pic", enabled = True),
        feature(name = "supports_dynamic_linker", enabled = True),
        feature(
            name = "opt",
            flag_sets = [
                flag_set(
                    actions = all_compile_actions,
                    flag_groups = [
                        flag_group(
                            flags = ["-O3"],
                        ),
                    ],
                ),
            ],
        ),
        feature(name = "dbg"),  # TODO add debug compile flags
        feature(
            name = "roborio_toolchain_feature",
            enabled = True,
            flag_sets = [
                flag_set(
                    actions = [
                        ACTION_NAMES.c_compile,
                        ACTION_NAMES.cpp_compile,
                    ],
                    flag_groups = [
                        flag_group(
                            flags = [
                                "-no-canonical-prefixes",
                            ],
                        ),
                    ],
                ),
                flag_set(
                    actions = [
                        ACTION_NAMES.cpp_link_executable,
                        ACTION_NAMES.cpp_link_dynamic_library,
                        ACTION_NAMES.cpp_link_nodeps_dynamic_library,
                    ],
                    flag_groups = [
                        flag_group(
                            flags = [
                                "-lstdc++",
                                "-lpthread",
                                "-lm",
                            ],
                        ),
                    ],
                ),
            ],
        ),
        feature(
            name = "compiler_param_file",
        ),
    ]

    return cc_common.create_cc_toolchain_config_info(
        ctx = ctx,
        toolchain_identifier = "roborio_toolchain",
        host_system_name = "local",
        target_system_name = "arm-frc2022-linux-gnueabi",
        target_cpu = "armv7",
        target_libc = "glibc-2.24",
        cc_target_os = "linux",
        compiler = "gcc-7.3.0",
        abi_version = "gcc-7.3.0",
        abi_libc_version = "glibc-2.24",
        tool_paths = tool_paths,
        builtin_sysroot = "external/athena_toolchain_%s_files/frc2022/roborio/arm-frc2022-linux-gnueabi" % ctx.attr.toolchain_host,
        features = features,
    )

athena_cc_toolchain_config = rule(
    implementation = _impl,
    attrs = {
        "toolchain_host": attr.string(),
    },
    provides = [CcToolchainConfigInfo],
)
