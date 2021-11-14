load("@rules_jvm_external//:private/coursier_utilities.bzl", "escape")

def artifact_list_to_genfile_targets(repo_name, artifacts):
    """Turns a list of maven artifacts into a list of genrule target labels for library and source jars

    When using artifact pinning, maven_install (part of rules_jvm_external) uses Bazel's http_file download mechanism
    instead of Coursier (also included in rules_jvm_external) to download maven artifacts.  This means instead of them
    all being centrally located, each jar file (and thus each sources jar file as well) lives in its own external
    workspace (bazel-AdvantageKit/external/...), making it challenging for VSCode's Java plugin to find them
    (it is especially difficult since the downloaded files do not have a file extension so they aren't treated as jars).
    Luckily, maven_install generates a BUILD file witin the @maven external repo (and @frcmaven and any others)
    with genrules to copy each downloaded file to the genfiles (bazel-out and bazel-bin) directories.

    This utility function takes in the name of the maven_install repo and a list of artifacts ("package:name:version")
    and returns a list of fully qualified genrule target labels ("@repo//:package_name_version_extension") for both the
    library and source jars of the artifact.  This list can be used in the deps section of another rule to execute
    all of the genrules and copy all of the files to the genfiles directory, so that VSCode or others can see them.

    Args:
        repo_name: Name of the external maven repo (same name as maven_install target)
        artifacts: List of maven artifact strings

    Returns:
        List of fully qualified genrule target names, each of which copies a single library or source file from
        its dedicated external http_file repo to the genfiles directory under the standard maven folder structure
    """
    ret = []
    for a in artifacts:
        artifact_split = a.split(":")
        package_escaped = escape(artifact_split[0])
        artifact_escaped = escape(artifact_split[1])
        version_escaped = escape(artifact_split[2])

        ret.append("@{}//:{}_{}_{}_extension".format(repo_name, package_escaped, artifact_escaped, version_escaped))
        ret.append("@{}//:{}_{}_jar_sources_{}_extension".format(repo_name, package_escaped, artifact_escaped, version_escaped))
    return ret
