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


# The filegroup and copy_filegroup rules are needed to put the headers in the genfiles directory (bazel-bin)
# so that they don't get deleted when running a new build operation, preventing VSCode from seeing them anymore.
# If we had clangd reference the files in the build root (bazel-AdvantageKit), which is a symlink to a hidden directory
# in the user's home folder, they would disappear as soon as a target that didn't require them was built, meaning
# VSCode could no longer provide code completion.  Additionally, if for some reason we couldn't run a successful build,
# the symlinks would never be created, so there would be no code completion while trying to fix the bug that broke the
# build (very bad!).  Creating these copy rules allows the required headers to be copied to the genfiles directory
# which isn't erased unless a clean is run, and allows a rule which invokes the copy rules without building any
# (potentially failing) source files to be created.
# TODO When we find better tooling for Bazel C/C++ targets in VSCode, we can remove this.
initial_build_template = """
load("@bazel_skylib//rules:copy_file.bzl", "copy_file")
"""

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

rename_template = """
copy_file(
    name = "{0}-copy",
    src = "{1}/{2}/{3}/{0}",
    out = "{1}/{2}/{3}/{4}",
)
"""

cc_import_template = """
cc_import(
    name = "{0}",
    {3}_library = "{1}/{2}/{3}/{0}",
    {4}
)
"""

binary_library_template = """
cc_library(
    name = "binaries",
    visibility = ["{0}"],
    deps = [{1}],
    alwayslink = True,
)
"""

# base_url + package + "/%s/%s/%s-%s-headers.zip" % (name, version, name, version)
url_template = "https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/{0}/{1}/{2}/{1}-{2}-{3}{4}.zip"

def wpilib_binary_config(platform, libs, sha256, is_static = False, windows_libs = {}, renames = {}):
    return (platform, libs, sha256, is_static, windows_libs, renames)

def wpilib_nativezip(name, remote_name, package, version, visibility, headers_sha256 = None, binary_configs = None):
    # Create the http_archive for the headers
    #https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/{package}/{name}/{version}/{name}-{version}-headers.zip
    if headers_sha256 != None:
        http_archive(
            name = name + "_headers_files",
            build_file_content = header_build_template.format("//visibility:public"),
            sha256 = headers_sha256,
            urls = [url_template.format(package, remote_name, version, "headers", "")],
        )

    if binary_configs != None:
        for config in binary_configs:
            # Get the WPILib os and arch names from our platform name
            platform_os = platform_mappings[config[0]][0]
            platform_arch = platform_mappings[config[0]][1]
            is_static = config[3]
            windows_libs = config[4]
            renames = config[5]

            # Generate the build file contents
            build_content = initial_build_template
            binary_targets = ""
            for lib in config[1]:
                if lib in renames:
                    lib_maybe_renamed = renames[lib]
                    build_content += rename_template.format(lib, platform_os, platform_arch, "static" if is_static else "shared", lib_maybe_renamed)
                else:
                    lib_maybe_renamed = lib
                
                # Generate the cc_import rule for each library file
                if lib in windows_libs:
                    if windows_libs[lib] in renames:
                        windows_lib_maybe_renamed = renames[windows_libs[lib]]
                        build_content += rename_template.format(windows_libs[lib], platform_os, platform_arch, "shared", windows_lib_maybe_renamed)
                    else:
                        windows_lib_maybe_renamed = windows_libs[lib]

                    interface_lib = "interface_library = \"{1}/{2}/shared/{0}\"".format(windows_lib_maybe_renamed, platform_os, platform_arch)
                else:
                    interface_lib = ""

                build_content += cc_import_template.format(lib_maybe_renamed, platform_os, platform_arch, "static" if is_static else "shared", interface_lib)

                # Add the cc_import target to the string
                binary_targets += "\":%s\"," % lib_maybe_renamed

            # Generate the final cc_library target
            build_content += binary_library_template.format(visibility, binary_targets)

            # Create the http_archive for the binary zip
            http_archive(
                name = name + "_%s_files" % config[0],
                build_file_content = build_content,
                sha256 = config[2],
                urls = [url_template.format(package, remote_name, version, platform_os + platform_arch, "static" if is_static else "")],
            )
