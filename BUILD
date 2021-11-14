# A filegroup target containing all of the downloads required for VSCode code completion for both Java and C++ targets.
# Running "bazel build :vscode" will download the needed files to allow it to work.
load("//build_tools/repo:maven.bzl", "artifact_list_to_genfile_targets")
load(":library_deps.bzl", "MAVEN_ARTIFACTS", "FRCMAVEN_ARTIFACTS")

maven_genfile_targets = artifact_list_to_genfile_targets("maven", MAVEN_ARTIFACTS) + artifact_list_to_genfile_targets("frcmaven", FRCMAVEN_ARTIFACTS)

filegroup(
    name = "vscode",
    srcs = [
        # WPILibJ (special case, downloaded seperately)
        "//third_party/wpilib:wpilibj_jar_genfile",
        "//third_party/wpilib:wpilibj_sources_jar_genfile",

        # C/C++ headers
        "@ni_visa_headers_files//:headers_genfiles",
        "@ni_netcomm_headers_files//:headers_genfiles",
        "@ni_chipobject_headers_files//:headers_genfiles",
        "@wpilib_wpiutil_headers_files//:headers_genfiles",
        "@wpilib_hal_headers_files//:headers_genfiles",
    ] + maven_genfile_targets,
)
