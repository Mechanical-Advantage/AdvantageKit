load("//build_tools/cc:platform_dynamic_library.bzl", "cc_platform_dynamic_library")

cc_library(
    name = "wpilibio-lib",
    srcs = glob(["src/**/*.cc"]),
    hdrs = glob(["include/**/*.h"]),
    linkstatic = True,  # Only statically link this library since we want it to statically link into the dynamic lib below
    visibility = ["//visibility:public"],
    deps = [
        "//conduit:conduit-schema-cc",
        "//conduit/api:api-jni",
        "//third_party/wpilib:hal",
    ],
    alwayslink = True,
)

cc_platform_dynamic_library(
    name = "wpilibio-shared",
    libname = "wpilibio",
    visibility = ["//visibility:public"],
    deps = [":wpilibio-lib"],
)
