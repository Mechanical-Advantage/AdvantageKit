def _copy_cc_headers_impl(ctx):
    inputs = ctx.attr.cc_target[CcInfo].compilation_context.direct_headers
    all_outputs = []
    for f in inputs:
        out = ctx.actions.declare_file(f.path)
        all_outputs.append(out)
        ctx.actions.run_shell(
            outputs = [out],
            inputs = depset([f]),
            arguments = [f.path, out.path],
            command = "cp $1 $2",
        )

    if len(inputs) != len(all_outputs):
        fail("Output count should be 1-to-1 with input count.")

    return [
        DefaultInfo(
            files = depset(all_outputs),
            runfiles = ctx.runfiles(files = all_outputs),
        ),
    ]

copy_cc_headers = rule(
    implementation = _copy_cc_headers_impl,
    attrs = {
        "cc_target": attr.label(),
    },
)

def _copy_filegroup_impl(ctx):
    """Copies all files in a filegroup to the genfiles (bazel-out) directory

    The copied files maintain the folder structure they had relative to the repository root of where they came from.
    Example:
        inputs: "@repo1//some/folder:file1.txt"
                "@repo2//some/other/folder:file2.txt"
                "//some/other/other/folder:file3.txt"
        outputs:
                "bazel-bin/external/repo1/some/folder/file1.txt"
                "bazel-bin/external/repo2/some/other/folder/file2.txt"
                "bazel-bin/some/other/other/folder/file3.txt"

    This rule uses run_shell, which uses the same shell priorties as genrules.  This means it should work on any system
    with Bash or PowerShell.
    """
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
    },
)
