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

cc_library(
    name = "wpiutil-athena",
    visibility = ["//visibility:public"],
    deps = [
        "@wpilib_wpiutil_athena_files//:binaries",
        "@wpilib_wpiutil_headers_files//:headers",
    ],
)

cc_library(
    name = "wpiutil-linux_x64",
    visibility = ["//visibility:public"],
    deps = [
        "@wpilib_wpiutil_headers_files//:headers",
        "@wpilib_wpiutil_linux_x64_files//:binaries",
    ],
)

cc_library(
    name = "hal-athena",
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
    visibility = ["//visibility:public"],
    deps = [
        ":wpiutil-linux_x64",
        "@wpilib_hal_headers_files//:headers",
        "@wpilib_hal_linux_x64_files//:binaries",
    ],
)
