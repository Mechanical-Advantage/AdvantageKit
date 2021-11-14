load("@rules_jvm_external//:private/coursier_utilities.bzl", "escape")

def artifact_list_to_genfile_targets(repo_name, artifacts):
    ret = []
    for a in artifacts:
        artifact_split = a.split(":")
        package_escaped = escape(artifact_split[0])
        artifact_escaped = escape(artifact_split[1])
        version_escaped = escape(artifact_split[2])
        
        ret.append("@{}//:{}_{}_{}_extension".format(repo_name, package_escaped, artifact_escaped, version_escaped))
        ret.append("@{}//:{}_{}_jar_sources_{}_extension".format(repo_name, package_escaped, artifact_escaped, version_escaped))
    return ret