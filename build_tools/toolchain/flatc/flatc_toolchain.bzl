def _flatc_toolchain_impl(ctx):
    toolchain_info = platform_common.ToolchainInfo(
        flatc_executable = ctx.executable.flatc
    )

    return [toolchain_info]

flatc_toolchain = rule(
    implementation = _flatc_toolchain_impl,
    attrs = {
        "flatc": attr.label(mandatory = True, allow_single_file = True, executable = True, cfg = "exec")
    }
)