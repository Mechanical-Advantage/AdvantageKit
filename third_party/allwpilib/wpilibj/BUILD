java_library(
    name = "wpilibj",
    srcs = glob(["src/main/java/**/*.java"]),
    visibility = ["//visibility:public"],
    javacopts = ["-Xlint:-removal", "-Xlint:-dep-ann"],
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
        "@maven//:org_ejml_ejml_core"
    ],
)
