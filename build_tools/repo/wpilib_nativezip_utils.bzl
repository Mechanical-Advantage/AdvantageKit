load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

# WPILib releases headers and binaries of native libraries in zip files hosted on their Artifactory server.
# The standard is that each component is released in several zip files: a headers zip, a sources zip (not needed),
# and a binary zip for each supported platform (linuxathena, linux-x86-64, etc.)

# Each file needs to be registered as an http_archive, and some basic rules need to be created to gather the files
# inside each downloaded archive into Bazel targets (cc_library for headers, cc_library for binaries) so that
# higher level rules in //third_party can further combine them into useful targets.  Since there are many files
# that need to be downloaded, doing ths manually for each is difficult to maintain.  Thus, a macro is provided here
# which takes care of registering all zips for a given component with Bazel, and making their contents available.

# We use different strings to represent OS and CPU arch.  This dictionary maps our names to WPILib's
# in the form of a tuple, where element 0 is the operating system, and element 1 is the architecture.
# URLs for the zip files are of the form "...{os}{arch}.zip"
platform_mappings = {
    "athena": ("linux", "athena"),
    "linux_x64": ("linux", "x86-64"),
    "windows_x64": ("windows", "x86-64"),
    "macos_x64": ("osx", "x86-64"),
}

header_build_template = """
load("@//build_tools/repo:copy_filegroup.bzl", "copy_filegroup")

filegroup(
    name = "headers_files",
    srcs = glob([
        "**/*.h",
        "**/*.hpp",
    ]),
    visibility = ["//visibility:public"]
)

copy_filegroup(
    name = "headers_genfiles",
    target_filegroups = [":headers_files"],
    visibility = ["//visibility:public"]
)

cc_library(
    name = "headers",
    hdrs = [":headers_files"],
    includes = ["."],
    visibility = ["{0}"],
)
"""

cc_import_template = """
cc_import(
    name = "{0}",
    shared_library = "{1}/{2}/shared/{0}"
)
"""

binary_library_template = """
cc_library(
    name = "binaries",
    visibility = ["{0}"],
    deps = [{1}],
)
"""

# base_url + package + "/%s/%s/%s-%s-headers.zip" % (name, version, name, version)
url_template = "https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/{0}/{1}/{2}/{1}-{2}-{3}.zip"

def wpilib_binary_config(platform, libs, sha256):
    return (platform, libs, sha256)

def wpilib_nativezip(name, remote_name, package, version, visibility, headers_sha256 = None, binary_configs = None):
    # Create the http_archive for the headers
    #https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/{package}/{name}/{version}/{name}-{version}-headers.zip
    if headers_sha256 != None:
        http_archive(
            name = name + "_headers_files",
            build_file_content = header_build_template.format("//visibility:public"),
            sha256 = headers_sha256,
            urls = [url_template.format(package, remote_name, version, "headers")],
        )

    if binary_configs != None:
        for config in binary_configs:
            # Get the WPILib os and arch names from our platform name
            platform_os = platform_mappings[config[0]][0]
            platform_arch = platform_mappings[config[0]][1]

            # Generate the build file contents
            build_content = ""
            binary_targets = ""
            for lib in config[1]:
                # Generate the cc_import rule for each library file
                build_content += cc_import_template.format(lib, platform_os, platform_arch)

                # Add the cc_import target to the string
                binary_targets += "\":%s\"," % lib

            # Generate the final cc_library target
            build_content += binary_library_template.format(visibility, binary_targets)

            # Create the http_archive for the binary zip
            http_archive(
                name = name + "_%s_files" % config[0],
                build_file_content = build_content,
                sha256 = config[2],
                urls = [url_template.format(package, remote_name, version, platform_os + platform_arch)],
            )
