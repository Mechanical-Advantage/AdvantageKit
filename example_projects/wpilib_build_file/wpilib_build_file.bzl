load("//:library_deps.bzl", "WPILIB_VERSION")

def _impl(ctx):
    substitutions = {
        "{akit_version}": ctx.var["publishing_version"],
        "{wpilib_version}": WPILIB_VERSION,
    }
    content = ctx.actions.declare_file("build.gradle")
    ctx.actions.expand_template(
        template = ctx.file._template,
        output = content,
        substitutions = substitutions,
    )

    return DefaultInfo(
        files = depset([content]),
    )

wpilib_build_file = rule(
    implementation = _impl,
    attrs = {
        "_template": attr.label(default = "@//example_projects/wpilib_build_file:template", allow_single_file = True),
    },
)
