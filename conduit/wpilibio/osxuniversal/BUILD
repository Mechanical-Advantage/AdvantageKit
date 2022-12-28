# !!! THIS IS VERY VERY TEMPORARY
# !!! WE NEED TO CLEAN THIS UP
# !!! TODO

load("@build_bazel_apple_support//rules:universal_binary.bzl", "universal_binary")
load("//build_tools/repo:maven_publish_zip.bzl", "maven_publish_zip")
load("@rules_pkg//:pkg.bzl", "pkg_zip")

universal_binary(
    name = "libwpilibio.dylib",
    binary = "//conduit/wpilibio:shared",
    tags = ["manual"],
)

pkg_zip(
    name = "nativezip",
    srcs = [":libwpilibio.dylib"],
    package_dir = "/osx/universal/shared",
    package_file_name = "wpilibio-{publishing_version}-osxuniversal.zip",
    package_variables = "//build_tools/zip:version",
    tags = ["manual"],
)

maven_publish_zip(
    name = "nativezip.publish",
    classifier = "osxuniversal",
    coordinates = "org.littletonrobotics.akit.conduit:conduit-wpilibio:{publishing_version}",
    tags = ["manual"],
    zip_file = ":nativezip",
)
