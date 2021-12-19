load("//build_tools/cc:platform_dynamic_library.bzl", "cc_platform_dynamic_library")
load("//build_tools/repo:maven_publish_zip.bzl", "maven_publish_zip")
load("@rules_pkg//:pkg.bzl", "pkg_zip")

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

pkg_zip(
    name = "wpilibio-nativezip",
    srcs = [":wpilibio-shared"],
    package_dir = select({
        "//build_tools/platforms:is_athena": "/linux/athena/shared",
        "//build_tools/platforms:is_linux": "/linux/x86-64/shared",
        "//build_tools/platforms:is_windows": "/windows/x86-64/shared",
        "//build_tools/platforms:is_macos": "/osx/x86-64/shared",
    }),
    package_file_name = select({
        "//build_tools/platforms:is_athena": "wpilibio-{publishing_version}-athena.zip",
        "//build_tools/platforms:is_linux": "wpilibio-{publishing_version}-linuxx86-64.zip",
        "//build_tools/platforms:is_windows": "wpilibio-{publishing_version}-windowsx86-64.zip",
        "//build_tools/platforms:is_macos": "wpilibio-{publishing_version}-osxx86-64.zip",
    }),
    package_variables = "//build_tools/zip:version",
)

maven_publish_zip(
    name = "wpilibio-nativezip-publish",
    coordinates = "org.littletonrobotics.akit.conduit:conduit-wpilibio:{publishing_version}",
    zip_file = ":wpilibio-nativezip",
    classifier = select({
        "//build_tools/platforms:is_athena": "athena",
        "//build_tools/platforms:is_linux": "linuxx86-64",
        "//build_tools/platforms:is_windows": "windowsx86-64",
        "//build_tools/platforms:is_macos": "osxx86-64"
    })
)