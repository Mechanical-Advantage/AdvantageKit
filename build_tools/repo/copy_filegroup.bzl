def _copy_filegroup_impl(ctx):
    all_input_files = [
        f
        for t in ctx.attr.target_filegroups
        for f in t.files.to_list()
    ]

    all_outputs = []
    for f in all_input_files:
        out = ctx.actions.declare_file(f.short_path)
        all_outputs.append(out)
        ctx.actions.run_shell(
            outputs = [out],
            inputs = depset([f]),
            arguments = [f.path, out.path],
            command = "cp $1 $2",
        )

    if len(all_input_files) != len(all_outputs):
        fail("Output count should be 1-to-1 with input count.")

    return [
        DefaultInfo(
            files = depset(all_outputs),
            runfiles = ctx.runfiles(files = all_outputs),
        ),
    ]

copy_filegroup = rule(
    implementation = _copy_filegroup_impl,
    attrs = {
        "target_filegroups": attr.label_list(),
    }
)