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
    mode = "0777",
)

pkg_files(
    name = "diff_drive_files",
    srcs = glob(
        ["diff_drive/**/*"],
    ),
    strip_prefix = strip_prefix.from_pkg("diff_drive"),
)

pkg_zip(
    name = "diff_drive",
    srcs = [
        "diff_drive_files",
        "template_build_file",
        "template_files",
        "template_vendordep",
    ],
    mode = "0777",
)

pkg_files(
    name = "swerve_drive_files",
    srcs = glob(
        ["swerve_drive/**/*"],
    ),
    strip_prefix = strip_prefix.from_pkg("swerve_drive"),
)

pkg_zip(
    name = "swerve_drive",
    srcs = [
        "swerve_drive_files",
        "template_build_file",
        "template_files",
        "template_vendordep",
    ],
    mode = "0777",
)

pkg_files(
    name = "advanced_swerve_drive_files",
    srcs = glob(
        ["advanced_swerve_drive/**/*"],
    ),
    strip_prefix = strip_prefix.from_pkg("advanced_swerve_drive"),
)

pkg_zip(
    name = "advanced_swerve_drive",
    srcs = [
        "advanced_swerve_drive_files",
        "template_build_file",
        "template_files",
        "template_vendordep",
    ],
    mode = "0777",
)

pkg_files(
    name = "kitbot_2024_files",
    srcs = glob(
        ["kitbot_2024/**/*"],
    ),
    strip_prefix = strip_prefix.from_pkg("kitbot_2024"),
)

pkg_zip(
    name = "kitbot_2024",
    srcs = [
        "kitbot_2024_files",
        "template_build_file",
        "template_files",
        "template_vendordep",
    ],
    mode = "0777",
)

filegroup(
    name = "example_projects",
    srcs = [
        "advanced_swerve_drive",
        "diff_drive",
        "kitbot_2024",
        "skeleton",
        "swerve_drive",
    ],
)
