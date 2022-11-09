load("@rules_pkg//:mappings.bzl", "pkg_files", "strip_prefix")
load("@rules_pkg//:pkg.bzl", "pkg_zip")
load("//example_projects/wpilib_build_file:wpilib_build_file.bzl", "wpilib_build_file")

pkg_files(
    name = "template_files",
    srcs = glob(
        ["template/**/*"],
    ),
    strip_prefix = strip_prefix.from_pkg("template"),
)

pkg_files(
    name = "template_vendordep",
    srcs = ["//:vendordep"],
    prefix = "vendordeps",
)

wpilib_build_file(
    name = "template_build_file",
)

pkg_files(
    name = "skeleton_files",
    srcs = glob(
        ["skeleton/**/*"],
    ),
    strip_prefix = strip_prefix.from_pkg("skeleton"),
)

pkg_zip(
    name = "skeleton",
    srcs = [
        "skeleton_files",
        "template_build_file",
        "template_files",
        "template_vendordep",
    ],
)

pkg_files(
    name = "command_files",
    srcs = glob(
        ["command/**/*"],
    ),
    strip_prefix = strip_prefix.from_pkg("command"),
)

pkg_zip(
    name = "command",
    srcs = [
        "command_files",
        "template_build_file",
        "template_files",
        "template_vendordep",
    ],
)

filegroup(
    name = "example_projects",
    srcs = [
        "command",
        "skeleton",
    ],
)
