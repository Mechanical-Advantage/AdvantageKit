load("@bazel_tools//tools/cpp:toolchain_utils.bzl", "find_cpp_toolchain")

def impl(ctx):
    # Straight from Bazel docs, this is how to be compatible with platforms
    cc_toolchain = find_cpp_toolchain(ctx)
    feature_config = cc_common.configure_features(
        ctx = ctx,
        cc_toolchain = cc_toolchain,
        requested_features = ctx.features,
        unsupported_features = ctx.disabled_features,
    )

    linker_inputs = []
    runfile_symlinks = {}
    for dynamic_lib in ctx.files.dynamic_libs:
        if dynamic_lib.basename.endswith("debug"):
            continue
        library_to_link = cc_common.create_library_to_link(
            actions = ctx.actions,
            feature_configuration = feature_config,
            cc_toolchain = cc_toolchain,
            dynamic_library = dynamic_lib,
            alwayslink = ctx.attr.alwayslink,
        )
        linker_input = cc_common.create_linker_input(
            owner = ctx.label,
            libraries = depset([library_to_link])
        )
        linker_inputs.append(linker_input)

        # Symlink the dynamic library to a specific path in runfiles
        # This fixes an issue in macOS where the rpath isn't being set correctly
        # and also makes JNI loading work properly for imported libraries
        runfile_symlinks["__main__/" + dynamic_lib.basename] = dynamic_lib
    linking_context = cc_common.create_linking_context(
        linker_inputs = depset(linker_inputs),
    )

    runfiles = ctx.runfiles(
        root_symlinks = runfile_symlinks
    )

    transitive_runfiles_list = []

    this_cc_info = CcInfo(
        linking_context = linking_context,
    )
    cc_infos = [this_cc_info]

    for dep in ctx.attr.deps:
        cc_infos.append(dep[CcInfo])
        transitive_runfiles_list.append(dep[DefaultInfo].default_runfiles)
    merged_cc_info = cc_common.merge_cc_infos(
        cc_infos = cc_infos,
    )

    for maybe_runfiles in transitive_runfiles_list:
        if maybe_runfiles:
            runfiles = runfiles.merge(maybe_runfiles)
    
    return [merged_cc_info, DefaultInfo(runfiles = runfiles)]


cc_superimport = rule(
    implementation = impl,
    attrs = {
        "dynamic_libs": attr.label(mandatory = True, allow_files = True),
        "interface_libs": attr.label(allow_files = True),
        "deps": attr.label_list(),
        "alwayslink": attr.bool(default = False),
        # This is from the Bazel docs
        "_cc_toolchain": attr.label(default = "@bazel_tools//tools/cpp:current_cc_toolchain"),
    },
    toolchains = ["@bazel_tools//tools/cpp:toolchain_type"],
    fragments = ["cpp"],
)
