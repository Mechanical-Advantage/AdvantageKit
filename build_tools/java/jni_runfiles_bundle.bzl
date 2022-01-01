def _impl(ctx):
    runfile_symlinks = {}
    for dep in ctx.attr.deps:
        if CcInfo in dep:
            for linker_input in dep[CcInfo].linking_context.linker_inputs.to_list():
                if hasattr(linker_input.libraries, "to_list"):
                    libs = linker_input.libraries.to_list()
                else:
                    libs = linker_input.libraries
                for lib in libs:
                    if lib.dynamic_library:
                        runfile_symlinks["__main__/" + lib.dynamic_library.basename] = lib.dynamic_library
    for f in ctx.files.dynamic_libs:
        runfile_symlinks["__main__/" + f.basename] = f
    
    runfiles = ctx.runfiles(
        root_symlinks = runfile_symlinks
    )
    
    return [DefaultInfo(runfiles = runfiles)]

jni_runfiles_bundle = rule(
    implementation = _impl,
    attrs = {
        "deps": attr.label_list(mandatory = True),
        "dynamic_libs": attr.label_list()
    }
)