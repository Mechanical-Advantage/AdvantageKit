def _impl(ctx):
    patch_jars = [jar.class_jar for jar in ctx.attr.patch_lib[JavaInfo].outputs.jars]
    orig_jars = [jar.class_jar for jar in ctx.attr.orig_lib[JavaInfo].outputs.jars]

    print(ctx.executable._sigcheck.path)

    ctx.actions.write(
        output = ctx.outputs.executable,
        is_executable = True,
        content = ctx.expand_location("$(locations //build_tools/jpatch:sigcheck)", [ctx.attr._sigcheck])
    )

    #ctx.actions.run(
    #    inputs = []
    #)

    #orig_lib_
    #ctx.actions.run(
    #    inputs = [x.class_jar for x in ctx.attr.lib[JavaInfo].outputs.jars]
    #)
    #print(ctx.attr.lib[JavaInfo].outputs.jars[0].class_jar)
    

java_sigcheck_test = rule(
    implementation = _impl,
    attrs = {
        "patch_lib": attr.label(mandatory = True),
        "orig_lib": attr.label(mandatory = True),
        "_sigcheck": attr.label(executable = True, cfg = "host", default = Label("//build_tools/jpatch:sigcheck"), allow_files = True)
    },
    test = True
)