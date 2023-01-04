from dataclasses import dataclass
from typing import Mapping
import urllib.request
import json
import os

# This script generates library_deps.bzl.  We use this script to fetch sha256 checksums of all dependencies
# and capture them within the generated file.

# Currently modifyable parameters:
# NI_VERSION
# WPILIB_VERSION
# MAVEN_DEPS -> standard maven dependencies go here
# WPILIB_JAVA_DEPS -> frcmaven java dependencies go here
# NATIVE_DEPS -> nativezip dependencies go here

# IMPORTANT:
# After updating anything, re-run the script to regenerate library_deps.bzl

# URL of the frcmaven maven server.
FRC_ARTIFACTORY_URL = "https://frcmaven.wpi.edu/artifactory"

# Version of WPILib dependencies to use
NI_VERSION = "2023.3.0"
WPILIB_VERSION = "2023.1.1"

# --- Artifacts and versions ---
# Dependencies to pull from standard maven repositories (currently just central)
MAVEN_DEPS = [
    "io.github.classgraph:classgraph:4.8.128",
    "com.fasterxml.jackson.core:jackson-annotations:2.10.0",
    "com.fasterxml.jackson.core:jackson-core:2.10.0",
    "com.fasterxml.jackson.core:jackson-databind:2.10.0",
    "org.ejml:ejml-simple:0.38",
    "org.ejml:ejml-core:0.38",
    "junit:junit:4.13.2",
    "com.squareup:javapoet:1.13.0"
]

# WPILib dependencies to pull from frcmaven.  If no version is provided, the above version is used.
WPILIB_JAVA_DEPS = [
    "edu.wpi.first.hal:hal-java",
    "edu.wpi.first.wpiutil:wpiutil-java",
    "edu.wpi.first.wpimath:wpimath-java",
    "edu.wpi.first.ntcore:ntcore-java",
    "edu.wpi.first.cscore:cscore-java",
    "edu.wpi.first.cameraserver:cameraserver-java",
    "edu.wpi.first.thirdparty.frc2023.opencv:opencv-java:4.6.0-2",
]

# Platform string for athena: (ours, frcmaven's)
NATIVE_PLATFORM_ATHENA = ("athena", "linuxathena")

# Platform strings for desktop platforms: (ours, frcmaven's)
NATIVE_PLATFORM_DESKTOP = [
    ("linux_x64", "linuxx86-64"),
    ("windows_x64", "windowsx86-64"),
    ("macos_universal", "osxuniversal")
]


@dataclass
class NativeArtifactSpec:
    """Spec for a native artifact

    name: The name to use for the Bazel external repository, which is of the form: [name]_[platform/"headers"]_files
    remote_name: The name of the artifact on Artifactory (e.g. wpiutil-cpp)
    remote_package: The package of the artifact on Artifactory (e.g. wpiutil)
    version: The version of the artifact
    headers: True if the artifact has a headers zip
    athena: True if the artifact has an athena zip
    desktop: True if the artifact has zips for desktop platforms
    visibility: The visibility string to inject into generated Bazel rules
    """
    name: str
    remote_name: str
    remote_package: str
    version: str = WPILIB_VERSION
    headers: bool = True
    athena: bool = True
    desktop: bool = True
    visibility: str = "//visibility:public"


NATIVE_DEPS = [
    NativeArtifactSpec(
        name="ni_visa",
        remote_name="visa",
        remote_package="ni-libraries",
        version=NI_VERSION,
        desktop=False,
        visibility="@//third_party/ni:__pkg__"
    ),

    NativeArtifactSpec(
        name="ni_netcomm",
        remote_name="netcomm",
        remote_package="ni-libraries",
        version=NI_VERSION,
        desktop=False,
        visibility="@//third_party/ni:__pkg__"
    ),

    NativeArtifactSpec(
        name="ni_chipobject",
        remote_name="chipobject",
        remote_package="ni-libraries",
        version=NI_VERSION,
        desktop=False,
        visibility="@//third_party/ni:__pkg__"
    ),

    NativeArtifactSpec(
        name="ni_runtime",
        remote_name="runtime",
        remote_package="ni-libraries",
        version=NI_VERSION,
        headers=False,
        desktop=False,
        visibility="@//third_party/ni:__pkg__"
    ),

    NativeArtifactSpec(
        name="wpilib_wpiutil",
        remote_name="wpiutil-cpp",
        remote_package="wpiutil",
        visibility="@//third_party/wpilib:__pkg__"
    ),

    NativeArtifactSpec(
        name="wpilib_wpimath",
        remote_name="wpimath-cpp",
        remote_package="wpimath",
        visibility="@//third_party/wpilib:__pkg__"
    ),

    NativeArtifactSpec(
        name="wpilib_ntcore",
        remote_name="ntcore-cpp",
        remote_package="ntcore",
        visibility="@//third_party/wpilib:__pkg__"
    ),

    NativeArtifactSpec(
        name="wpilib_hal",
        remote_name="hal-cpp",
        remote_package="hal",
        visibility="@//third_party/wpilib:__pkg__"
    ),

    NativeArtifactSpec(
        name="wpilib_halsim_gui",
        remote_name="halsim_gui",
        remote_package="halsim",
        headers=False,
        athena=False,
        visibility="@//third_party/wpilib:__pkg__"
    )
]

# --- Templates ---
# Template for wpilibj jar and sources, which are handled separately from other maven dependencies
wpilibj_download_template = f"""
    http_file(
        name = "wpilibj_jar_file",
        downloaded_file_path = "wpilibj.jar",  # java_import needs the filename to contain .jar
        sha256 = "{{jar_sha256}}",
        urls = ["{FRC_ARTIFACTORY_URL}/{{jar_url}}"],
    )

    http_file(
        name = "wpilibj_sources_jar_file",
        downloaded_file_path = "wpilibj_sources.jar",
        sha256 = "{{sources_sha256}}",
        urls = ["{FRC_ARTIFACTORY_URL}/{{sources_url}}"],
    )
"""

# Template for http_archive repository rules build_file_content
# Creates a filegroup and cc_library for the headers inside a downloaded nativezip
native_header_build_content_template = """
load("@//build_tools/repo:copy_filegroup.bzl", "copy_filegroup")

filegroup(
    name = "headers_files",
    srcs = glob([
        "**/*.h",
        "**/*.hpp",
    ]),
    visibility = ["//visibility:public"]
)

copy_filegroup(
    name = "headers_genfiles",
    target_filegroups = [":headers_files"],
    visibility = ["//visibility:public"]
)

cc_library(
    name = "headers",
    hdrs = [":headers_files"],
    includes = ["."],
    visibility = ["{visibility}"],
)
"""

# Template for http_archive repository rules build_file_content
# Creates filegroups for the shared libraries inside a downloaded nativezip
native_binary_build_content_template = """
filegroup(
    name = "binaries_files",
    srcs = glob([
        "**/*.so*",
        "**/*.dylib",
        "**/*.dll",
    ]),
    visibility = ["{visibility}"],
)

filegroup(
    name = "interface_binaries_files",
    srcs = glob([
        "**/*.lib",
    ]),
    visibility = ["{visibility}"],
)
"""

native_url_template = "release/edu/wpi/first/{package}/{remote_name}/{version}/{remote_name}-{version}-{platform}.zip"

# Template for creating an http_archive for headers
http_archive_template = f"""
    http_archive(
        name = "{{name}}_{{platform}}_files",
        build_file_content = \"\"\"{{build_file}}\"\"\",
        sha256 = "{{sha256}}",
        urls = ["{FRC_ARTIFACTORY_URL}/{{url}}"]
    )
"""


file_template = f"""
# THIS FILE IS GENERATED.  DO NOT MODIFY (except to run the formatter).
# To modify the contents of this file, change the constants near the top of generate_library_deps.py, and then run that script with python3.

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive", "http_file")

MAVEN_ARTIFACTS = {{maven}}

WPILIB_VERSION = "{WPILIB_VERSION}"

FRCMAVEN_ARTIFACTS = {{frcmaven}}

def library_deps_setup():
{{wpilibj}}
{{native}}
"""

# --- Generator functions ---


def create_frcmaven():
    ret = []
    for artifact in WPILIB_JAVA_DEPS:
        split = artifact.split(":")
        if len(split) == 3:
            # We have a version
            ret.append(artifact)
        else:
            # Inject WPILib version
            ret.append(artifact + ":%s" % WPILIB_VERSION)
    return ret


def create_wpilibj():
    jar = f"release/edu/wpi/first/wpilibj/wpilibj-java/{WPILIB_VERSION}/wpilibj-java-{WPILIB_VERSION}.jar"
    sources = f"release/edu/wpi/first/wpilibj/wpilibj-java/{WPILIB_VERSION}/wpilibj-java-{WPILIB_VERSION}-sources.jar"
    jar_sha256 = get_artifact_sha256(jar)
    sources_sha256 = get_artifact_sha256(sources)

    return wpilibj_download_template.format(jar_sha256=jar_sha256, jar_url=jar, sources_sha256=sources_sha256, sources_url=sources)


def get_artifact_sha256(artifact: str) -> str:
    url = FRC_ARTIFACTORY_URL + "/api/storage/%s" % artifact
    with urllib.request.urlopen(url) as response:
        decoded = json.loads(response.read())
        sha256 = decoded["checksums"]["sha256"]
        print(f"Found SHA256 checksum '{sha256}' for artifact '{artifact}'")
        return decoded["checksums"]["sha256"]


def create_native_helper(spec: NativeArtifactSpec, local_platform: str, remote_platform: str, build_template: str) -> str:
    url = native_url_template.format(
        package=spec.remote_package,
        remote_name=spec.remote_name,
        version=spec.version,
        platform=remote_platform
    )
    sha256 = get_artifact_sha256(url)

    build_file = build_template.format(visibility=spec.visibility)

    return http_archive_template.format(
        name=spec.name,
        platform=local_platform,
        build_file=build_file,
        sha256=sha256,
        url=url
    )


def create_native() -> str:
    generated = ""
    # We need to possibly create an http_archive rule for headers, possibly one for athena, and possibly some for desktop.
    # Start with the build_file_content strings for each one.
    for spec in NATIVE_DEPS:
        if spec.headers:
            generated += create_native_helper(spec, "headers",
                                              "headers", native_header_build_content_template)
        if spec.athena:
            generated += create_native_helper(
                spec, NATIVE_PLATFORM_ATHENA[0], NATIVE_PLATFORM_ATHENA[1], native_binary_build_content_template)
        if spec.desktop:
            for platform in NATIVE_PLATFORM_DESKTOP:
                generated += create_native_helper(
                    spec, platform[0], platform[1], native_binary_build_content_template)

    return generated


if __name__ == "__main__":
    file_content = file_template.format(wpilibj=create_wpilibj(),
                                        native=create_native(), maven=MAVEN_DEPS, frcmaven=create_frcmaven())
    with open('library_deps.bzl', "w") as f:
        f.write(file_content)

    # Repin maven
    os.system("bazel run @unpinned_maven//:pin")
    os.system("bazel run @unpinned_frcmaven//:pin")
