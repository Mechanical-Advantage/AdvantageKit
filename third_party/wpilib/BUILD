cc_import(
    name = "hal-athena-binary",
    shared_library = "@wpilib_hal_athena//:shared",
)

cc_library(
    name = "wpiutil-athena",
    visibility = ["//visibility:public"],
    deps = [
        "@wpilib_wpiutil_athena//:wpilib-wpiutil-athena",
        "@wpilib_wpiutil_headers//:headers",
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
        "@wpilib_hal_athena//:wpilib-hal-athena",
        "@wpilib_hal_headers//:headers",
    ],
)
