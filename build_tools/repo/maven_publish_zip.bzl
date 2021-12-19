_TEMPLATE = """#!/usr/bin/env bash
echo "Uploading {coordinates}:{classifier} to {maven_repo}"
{uploader} {maven_repo} {user} {password} {coordinates} {classifier} {zip_file}
"""

def _maven_publish_impl(ctx):
    executable = ctx.actions.declare_file("%s-publisher" % ctx.attr.name)

    coordinates_substituted = ctx.attr.coordinates.format(publishing_version = ctx.var["publishing_version"])

    maven_repo = ctx.var.get("maven_repo", "''")
    user = ctx.var.get("maven_user", "''")
    password = ctx.var.get("maven_password", "''")

    ctx.actions.write(
        output = executable,
        is_executable = True,
        content = _TEMPLATE.format(
            uploader = ctx.executable._uploader.short_path,
            coordinates = coordinates_substituted,
            maven_repo = maven_repo,
            password = password,
            user = user,
            classifier = ctx.attr.classifier,
            zip_file = ctx.file.zip_file.short_path,
        ),
    )

    files = [
        ctx.file.zip_file,
    ]

    return [
        DefaultInfo(
            files = depset([executable]),
            executable = executable,
            runfiles = ctx.runfiles(
                files = files,
                collect_data = True,
            ).merge(ctx.attr._uploader[DefaultInfo].data_runfiles),
        ),
    ]

maven_publish_zip = rule(
    _maven_publish_impl,
    doc = """Publish artifacts to a maven repository.
The maven repository may accessed locally using a `file://` URL, or
remotely using an `https://` URL. The following flags may be set
using `--define`:
  gpg_sign: Whether to sign artifacts using GPG
  maven_repo: A URL for the repo to use. May be "https" or "file".
  maven_user: The user name to use when uploading to the maven repository.
  maven_password: The password to use when uploading to the maven repository.
When signing with GPG, the current default key is used.
""",
    executable = True,
    attrs = {
        "coordinates": attr.string(
            mandatory = True,
        ),
        "zip_file": attr.label(
            mandatory = True,
            allow_single_file = True,
        ),
        "_uploader": attr.label(
            executable = True,
            cfg = "host",
            default = "//build_tools/repo:maven-zip-publisher",
            allow_files = True,
        ),
        "classifier": attr.string()
    },
)
