load("@bazel_skylib//rules:copy_file.bzl", "copy_file")

copy_file(
    name = "wpilibj_jar_genfile",
    src = "@wpilibj_jar_file//file:wpilibj.jar",
    out = "downloaded/wpilibj.jar",
    visibility = ["//visibility:public"],
)

copy_file(
    name = "wpilibj_sources_jar_genfile",
    src = "@wpilibj_sources_jar_file//file:wpilibj_sources.jar",
    out = "downloaded/wpilibj-sources.jar",
    visibility = ["//visibility:public"],
)

java_import(
    name = "wpilibj",
    jars = ["@wpilibj_jar_file//file:wpilibj.jar"],
    srcjar = "@wpilibj_sources_jar_file//file:wpilibj_sources.jar",
    visibility = ["//visibility:public"],
    exports = [
        "@frcmaven//:edu_wpi_first_cameraserver_cameraserver_java",
        "@frcmaven//:edu_wpi_first_cscore_cscore_java",
        "@frcmaven//:edu_wpi_first_hal_hal_java",
        "@frcmaven//:edu_wpi_first_ntcore_ntcore_java",
        "@frcmaven//:edu_wpi_first_thirdparty_frc2021_opencv_opencv_java",
        "@frcmaven//:edu_wpi_first_wpimath_wpimath_java",
        "@frcmaven//:edu_wpi_first_wpiutil_wpiutil_java",
    ],
    deps = [
        "@frcmaven//:edu_wpi_first_cameraserver_cameraserver_java",
        "@frcmaven//:edu_wpi_first_cscore_cscore_java",
        "@frcmaven//:edu_wpi_first_hal_hal_java",
        "@frcmaven//:edu_wpi_first_ntcore_ntcore_java",
        "@frcmaven//:edu_wpi_first_thirdparty_frc2021_opencv_opencv_java",
        "@frcmaven//:edu_wpi_first_wpimath_wpimath_java",
        "@frcmaven//:edu_wpi_first_wpiutil_wpiutil_java",
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_fasterxml_jackson_core_jackson_core",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:org_ejml_ejml_simple",
    ],
)

java_library(
    name = "wpilibj-compileonly",
    neverlink = True,
    visibility = ["//visibility:public"],
    exports = [":wpilibj"],
)

cc_library(
    name = "wpiutil",
    visibility = ["//visibility:public"],
    deps = ["@wpilib_wpiutil_headers_files//:headers"] + select({
        "//build_tools/platforms:is_athena": ["@wpilib_wpiutil_athena_files//:binaries"],
        "//build_tools/platforms:is_linux": ["@wpilib_wpiutil_linux_x64_files//:binaries"],
        "//build_tools/platforms:is_windows": ["@wpilib_wpiutil_windows_x64_files//:binaries"],
        "//build_tools/platforms:is_macos": ["@wpilib_wpiutil_macos_x64_files//:binaries"],
    }),
)

cc_library(
    name = "ntcore",
    visibility = ["//visibility:public"],
    deps = select({
        "//build_tools/platforms:is_linux": ["@wpilib_ntcore_linux_x64_files//:binaries"],
        "//build_tools/platforms:is_windows": ["@wpilib_ntcore_windows_x64_files//:binaries"],
        "//build_tools/platforms:is_macos": ["@wpilib_ntcore_macos_x64_files//:binaries"],
    }),
)

cc_library(
    name = "wpimath",
    visibility = ["//visibility:public"],
    deps = select({
        "//build_tools/platforms:is_linux": ["@wpilib_wpimath_linux_x64_files//:binaries"],
        "//build_tools/platforms:is_windows": ["@wpilib_wpimath_windows_x64_files//:binaries"],
        "//build_tools/platforms:is_macos": ["@wpilib_wpimath_macos_x64_files//:binaries"],
    }),
)

cc_library(
    name = "hal",
    visibility = ["//visibility:public"],
    deps = [
        "@wpilib_hal_headers_files//:headers",
        ":wpiutil",
    ] + select({
        "//build_tools/platforms:is_athena": [
            "//third_party/ni:chipobject-athena",
            "//third_party/ni:netcomm-athena",
            "//third_party/ni:visa-athena",
            "@wpilib_hal_athena_files//:binaries",
        ],
        "//build_tools/platforms:is_linux": [
            "@wpilib_hal_linux_x64_files//:binaries",
        ],
        "//build_tools/platforms:is_windows": [
            "@wpilib_hal_windows_x64_files//:binaries",
        ],
        "//build_tools/platforms:is_macos": [
            "@wpilib_hal_macos_x64_files//:binaries",
        ],
    }),
)

# Special binary target which links in the hal binaries.
# WPILibJ tries to load a specific library with this name, so we need to
# match it instead of using Bazel's built in JNI loading system.
# This target can be used by java targets to bu
cc_binary(
    name = "wpiHaljni",
    linkshared = True,
    visibility = ["//visibility:public"],
    deps = [":hal"],
)

cc_binary(
    name = "halsim_gui",
    linkshared = True,
    visibility = ["//visibility:public"],
    deps = [
        ":wpimath",
        ":ntcore",
        ":wpiutil",
        ":hal",
    ] + select({
        "//build_tools/platforms:is_linux": ["@wpilib_halsim_gui_linux_x64_files//:binaries"],
        "//build_tools/platforms:is_windows": ["@wpilib_halsim_gui_windows_x64_files//:binaries"],
        "//build_tools/platforms:is_macos": ["@wpilib_halsim_gui_macos_x64_files//:binaries"],
    }),
)

cc_library(
    name = "wpiutil-athena",
    deprecation = "Use //third_party/wpilib:wpiutil instead (platform is auto detected).",
    visibility = ["//visibility:public"],
    deps = [
        "@wpilib_wpiutil_athena_files//:binaries",
        "@wpilib_wpiutil_headers_files//:headers",
    ],
)

cc_library(
    name = "wpiutil-linux_x64",
    deprecation = "Use //third_party/wpilib:wpiutil instead (platform is auto detected).",
    visibility = ["//visibility:public"],
    deps = [
        "@wpilib_wpiutil_headers_files//:headers",
        "@wpilib_wpiutil_linux_x64_files//:binaries",
    ],
)

cc_library(
    name = "wpiutil-windows_x64",
    deprecation = "Use //third_party/wpilib:wpiutil instead (platform is auto detected).",
    visibility = ["//visibility:public"],
    deps = [
        "@wpilib_wpiutil_headers_files//:headers",
        "@wpilib_wpiutil_windows_x64_files//:binaries",
    ],
)

cc_library(
    name = "wpiutil-macos_x64",
    deprecation = "Use //third_party/wpilib:wpiutil instead (platform is auto detected).",
    visibility = ["//visibility:public"],
    deps = [
        "@wpilib_wpiutil_headers_files//:headers",
        "@wpilib_wpiutil_macos_x64_files//:binaries",
    ],
)

cc_library(
    name = "hal-athena",
    deprecation = "Use //third_party/wpilib:hal instead (platform is auto detected).",
    visibility = ["//visibility:public"],
    deps = [
        ":wpiutil-athena",
        "//third_party/ni:chipobject-athena",
        "//third_party/ni:netcomm-athena",
        "//third_party/ni:visa-athena",
        "@wpilib_hal_athena_files//:binaries",
        "@wpilib_hal_headers_files//:headers",
    ],
)

cc_library(
    name = "hal-linux_x64",
    deprecation = "Use //third_party/wpilib:hal instead (platform is auto detected).",
    visibility = ["//visibility:public"],
    deps = [
        ":wpiutil-linux_x64",
        "@wpilib_hal_headers_files//:headers",
        "@wpilib_hal_linux_x64_files//:binaries",
    ],
)

cc_library(
    name = "hal-windows_x64",
    deprecation = "Use //third_party/wpilib:hal instead (platform is auto detected).",
    visibility = ["//visibility:public"],
    deps = [
        ":wpiutil-windows_x64",
        "@wpilib_hal_headers_files//:headers",
        "@wpilib_hal_windows_x64_files//:binaries",
    ],
)

cc_library(
    name = "hal-macos_x64",
    deprecation = "Use //third_party/wpilib:hal instead (platform is auto detected).",
    visibility = ["//visibility:public"],
    deps = [
        ":wpiutil-macos_x64",
        "@wpilib_hal_headers_files//:headers",
        "@wpilib_hal_macos_x64_files//:binaries",
    ],
)
