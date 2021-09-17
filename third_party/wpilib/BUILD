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
