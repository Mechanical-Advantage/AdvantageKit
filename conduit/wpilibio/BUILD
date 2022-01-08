load("//build_tools/cc:platform_dynamic_library.bzl", "cc_platform_dynamic_library")
load("//build_tools/repo:maven_publish_zip.bzl", "maven_publish_zip")
load("//build_tools/repo:maven_publish_pom.bzl", "maven_publish_pom")
load("@rules_pkg//:pkg.bzl", "pkg_zip")

cc_library(
    name = "wpilibio",
    srcs = glob(["src/**/*.cc"]),
    hdrs = glob(["include/**/*.h"]),
    linkopts = select({
        "//build_tools/platforms:is_linux_any": ["-lpthread"],
        "//build_tools/platforms:is_macos": ["-lpthread"],
        "//conditions:default": [],
    }),
    linkstatic = True,  # Only statically link this library since we want it to statically link into the dynamic lib below
    tags = ["ci_build"],
    visibility = ["//visibility:public"],
    deps = [
        "//conduit:conduit-schema-cc",
        "//conduit/api:api-jni",
        "//third_party/wpilib:hal",
    ],
    alwayslink = True,
)

cc_platform_dynamic_library(
    name = "shared",
    libname = "wpilibio",
    visibility = ["//visibility:public"],
    deps = [":wpilibio"],
)

pkg_zip(
    name = "nativezip",
    srcs = [":shared"],
    package_dir = select({
        "//build_tools/platforms:is_athena": "/linux/athena/shared",
        "//build_tools/platforms:is_linux": "/linux/x86-64/shared",
        "//build_tools/platforms:is_windows": "/windows/x86-64/shared",
        "//build_tools/platforms:is_macos": "/osx/x86-64/shared",
    }),
    package_file_name = select({
        "//build_tools/platforms:is_athena": "wpilibio-{publishing_version}-linuxathena.zip",
        "//build_tools/platforms:is_linux": "wpilibio-{publishing_version}-linuxx86-64.zip",
        "//build_tools/platforms:is_windows": "wpilibio-{publishing_version}-windowsx86-64.zip",
        "//build_tools/platforms:is_macos": "wpilibio-{publishing_version}-osxx86-64.zip",
    }),
    package_variables = "//build_tools/zip:version",
)

maven_publish_zip(
    name = "nativezip.publish",
    classifier = select({
        "//build_tools/platforms:is_athena": "linuxathena",
        "//build_tools/platforms:is_linux": "linuxx86-64",
        "//build_tools/platforms:is_windows": "windowsx86-64",
        "//build_tools/platforms:is_macos": "osxx86-64",
    }),
    coordinates = "org.littletonrobotics.akit.conduit:conduit-wpilibio:{publishing_version}",
    tags = [
        "ci_publish",
        "ci_publish_desktop",
    ],
    zip_file = ":nativezip",
)

maven_publish_pom(
    name = "pom.publish",
    coordinates = "org.littletonrobotics.akit.conduit:conduit-wpilibio:{publishing_version}",
    tags = ["ci_publish"],
)
