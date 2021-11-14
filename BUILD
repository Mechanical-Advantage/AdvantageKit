# A filegroup target containing all of the downloads required for VSCode code completion for both Java and C++ targets.
# Running "bazel build :vscode" will download the needed files to allow it to work.

# C/C++ external headers will need to be added to the srcs list of the below filegroup, as well as any external java
# dependencies not downloaded through maven_install

# Additionally, if any additional maven_install targets are added in the future to support additional maven repositories,
# they will need to be added to the "maven_genfile_targets" variable.

# Makes use of downloaded files that have been copied to genfiles so that they are all in one place and don't get
# erased when a build is run on a target that doesn't depend on a specific library.
# For more info about this, see the comments in both of the files that are loaded below.
load("//build_tools/repo:maven.bzl", "artifact_list_to_genfile_targets")
load(":library_deps.bzl", "FRCMAVEN_ARTIFACTS", "MAVEN_ARTIFACTS")

# Get a list of genfile target names which provide the files in the bazel-bin directory that VSCode can see.
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
