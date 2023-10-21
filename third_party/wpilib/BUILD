load("@bazel_skylib//rules:copy_file.bzl", "copy_file")
load("//build_tools/cc:cc_superimport.bzl", "cc_superimport")

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
        "@frcmaven//:edu_wpi_first_thirdparty_frc2024_opencv_opencv_java",
        "@frcmaven//:edu_wpi_first_wpimath_wpimath_java",
        "@frcmaven//:edu_wpi_first_wpiutil_wpiutil_java",
    ],
    deps = [
        "@frcmaven//:edu_wpi_first_cameraserver_cameraserver_java",
        "@frcmaven//:edu_wpi_first_cscore_cscore_java",
        "@frcmaven//:edu_wpi_first_hal_hal_java",
        "@frcmaven//:edu_wpi_first_ntcore_ntcore_java",
        "@frcmaven//:edu_wpi_first_thirdparty_frc2024_opencv_opencv_java",
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

cc_superimport(
    name = "wpiutil",
    dynamic_libs = select({
        "//build_tools/platforms:is_athena": ["@wpilib_wpiutil_athena_files//:binaries_files"],
        "//build_tools/platforms:is_linux": ["@wpilib_wpiutil_linux_x64_files//:binaries_files"],
        "//build_tools/platforms:is_windows": ["@wpilib_wpiutil_windows_x64_files//:binaries_files"],
        "//build_tools/platforms:is_macos": ["@wpilib_wpiutil_macos_universal_files//:binaries_files"],
    }),
    interface_libs = select({
        "//build_tools/platforms:is_windows": ["@wpilib_wpiutil_windows_x64_files//:interface_binaries_files"],
        "//conditions:default": [],
    }),
    visibility = ["//visibility:public"],
    deps = ["@wpilib_wpiutil_headers_files//:headers"],
)

cc_superimport(
    name = "ntcore",
    dynamic_libs = select({
        "//build_tools/platforms:is_linux": ["@wpilib_ntcore_linux_x64_files//:binaries_files"],
        "//build_tools/platforms:is_windows": ["@wpilib_ntcore_windows_x64_files//:binaries_files"],
        "//build_tools/platforms:is_macos": ["@wpilib_ntcore_macos_universal_files//:binaries_files"],
        "//conditions:default": [],
    }),
    target_compatible_with = select({
        "//build_tools/platforms:is_athena": ["@platforms//:incompatible"],
        "//conditions:default": [],
    }),
    visibility = ["//visibility:public"],
)

cc_superimport(
    name = "wpimath",
    dynamic_libs = select({
        "//build_tools/platforms:is_linux": ["@wpilib_wpimath_linux_x64_files//:binaries_files"],
        "//build_tools/platforms:is_windows": ["@wpilib_wpimath_windows_x64_files//:binaries_files"],
        "//build_tools/platforms:is_macos": ["@wpilib_wpimath_macos_universal_files//:binaries_files"],
        "//conditions:default": [],
    }),
    target_compatible_with = select({
        "//build_tools/platforms:is_athena": ["@platforms//:incompatible"],
        "//conditions:default": [],
    }),
    visibility = ["//visibility:public"],
)

cc_superimport(
    name = "hal",
    dynamic_libs = select({
        "//build_tools/platforms:is_athena": ["@wpilib_hal_athena_files//:binaries_files"],
        "//build_tools/platforms:is_linux": ["@wpilib_hal_linux_x64_files//:binaries_files"],
        "//build_tools/platforms:is_windows": ["@wpilib_hal_windows_x64_files//:binaries_files"],
        "//build_tools/platforms:is_macos": ["@wpilib_hal_macos_universal_files//:binaries_files"],
    }),
    interface_libs = select({
        "//build_tools/platforms:is_windows": ["@wpilib_hal_windows_x64_files//:interface_binaries_files"],
        "//conditions:default": [],
    }),
    visibility = ["//visibility:public"],
    deps = [
        ":wpiutil",
        "@wpilib_hal_headers_files//:headers",
    ],
)
