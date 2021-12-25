_TEMPLATE = """
mvn deploy:deploy-file -DgroupId={group_id} -DartifactId={artifact_id} -Dversion={version} -DrepositoryId={repo_id} -Dpackaging=pom -Dfile={pom_file} -Durl={url} -DgeneratePom=false
"""

def _pom_file_impl(ctx):
    if ctx.attr.is_windows:
        executable = ctx.actions.declare_file("%s-publisher.bat" % ctx.attr.name)
    else:
        executable = ctx.actions.declare_file("%s-publisher" % ctx.attr.name)

    coordinates_substituted = ctx.attr.coordinates.format(publishing_version = ctx.var["publishing_version"])

    maven_repo = ctx.var.get("maven_repo", "''")
    maven_repo_id = ctx.var.get("maven_repo_id", "github")

    coordinates_split = coordinates_substituted.split(":")
    group_id = coordinates_split[0]
    artifact_id = coordinates_split[1]
    version = coordinates_split[2]

    substitutions = {
        "{groupId}": group_id,
        "{artifactId}": artifact_id,
        "{version}": version,
    }

    pom = ctx.actions.declare_file("%s.xml" % ctx.label.name)
    ctx.actions.expand_template(
        template = ctx.file.pom_template,
        output = pom,
        substitutions = substitutions,
    )

    ctx.actions.write(
        output = executable,
        is_executable = True,
        content = _TEMPLATE.format(
            group_id = group_id,
            artifact_id = artifact_id,
            version = version,
            url = maven_repo,
            pom_file = pom.short_path,
            repo_id = maven_repo_id,
        ),
    )

    files = [
        pom,
    ]

    return [
        DefaultInfo(
            files = depset([executable]),
            executable = executable,
            runfiles = ctx.runfiles(
                files = files,
                collect_data = True,
            ),
        ),
    ]

maven_publish_pom_def = rule(
    _pom_file_impl,
    executable = True,
    attrs = {
        "pom_template": attr.label(
            doc = "Template file to use for the pom.xml",
            default = "//build_tools/repo:generic-pom-template",
            allow_single_file = True,
        ),
        "is_windows": attr.bool(mandatory = True),
        "coordinates": attr.string(mandatory = True),
    },
)

def maven_publish_pom(name, **kwargs):
    maven_publish_pom_def(
        name = name,
        is_windows = select({
            "//build_tools/platforms:is_windows": True,
            "//conditions:default": False,
        }),
        **kwargs
    )
