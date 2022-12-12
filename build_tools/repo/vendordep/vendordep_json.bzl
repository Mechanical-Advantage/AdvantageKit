_JAVA_DEP_TEMPLATE = """        
        {{
            "groupId": "{groupId}",
            "artifactId": "{artifactId}",
            "version": "{version}"
        }}{comma}"""

_JNI_DEP_TEMPLATE = """        
        {{
            "groupId": "{groupId}",
            "artifactId": "{artifactId}",
            "version": "{version}",
            "skipInvalidPlatforms": false,
            "isJar": false,
            "validPlatforms": [
                "linuxathena",
                "windowsx86-64",
                "linuxx86-64",
                "osxuniversal"
            ]
        }}{comma}"""

def _impl(ctx):
    java_deps = ""
    jni_deps = ""
    name = ctx.attr.json_name

    for i, coordinates in enumerate(ctx.attr.java_coordinates):
        coordinates_substituted = coordinates.format(publishing_version = ctx.var["publishing_version"])
        coordinates_split = coordinates_substituted.split(":")
        group_id = coordinates_split[0]
        artifact_id = coordinates_split[1]
        version = coordinates_split[2]

        last = (i == len(ctx.attr.java_coordinates) - 1)
        comma = "\n" if last else ","

        java_dep = _JAVA_DEP_TEMPLATE.format(groupId = group_id, artifactId = artifact_id, version = version, comma = comma)
        java_deps += java_dep

    for i, coordinates in enumerate(ctx.attr.nativezip_coordinates):
        coordinates_substituted = coordinates.format(publishing_version = ctx.var["publishing_version"])
        coordinates_split = coordinates_substituted.split(":")
        group_id = coordinates_split[0]
        artifact_id = coordinates_split[1]
        version = coordinates_split[2]

        last = (i == len(ctx.attr.nativezip_coordinates) - 1)
        comma = "\n" if last else ","

        jni_dep = _JNI_DEP_TEMPLATE.format(groupId = group_id, artifactId = artifact_id, version = version, comma = comma)
        jni_deps += jni_dep

    substitutions = {
        "{name}": name,
        "{version}": ctx.var["publishing_version"],
        "{uuid}": ctx.attr.uuid,
        "{javaDependencies}": java_deps,
        "{jniDependencies}": jni_deps,
    }
    json = ctx.actions.declare_file("%s.json" % name)
    ctx.actions.expand_template(
        template = ctx.file._top_template,
        output = json,
        substitutions = substitutions,
    )

    return DefaultInfo(
        files = depset([json]),
    )

vendordep_json = rule(
    implementation = _impl,
    attrs = {
        "_top_template": attr.label(default = "@//build_tools/repo/vendordep:top-template", allow_single_file = True),
        "json_name": attr.string(mandatory = True),
        "java_coordinates": attr.string_list(),
        "nativezip_coordinates": attr.string_list(),
        "uuid": attr.string(),
    },
)
