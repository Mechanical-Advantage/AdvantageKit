
# THIS FILE IS GENERATED.  DO NOT MODIFY (except to run the formatter).
# To modify the contents of this file, change the constants near the top of generate_library_deps.py, and then run that script with python3.

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive", "http_file")

MAVEN_ARTIFACTS = ['io.github.classgraph:classgraph:4.8.128', 'com.fasterxml.jackson.core:jackson-annotations:2.10.0', 'com.fasterxml.jackson.core:jackson-core:2.10.0', 'com.fasterxml.jackson.core:jackson-databind:2.10.0', 'org.ejml:ejml-simple:0.38', 'org.ejml:ejml-core:0.38', 'junit:junit:4.13.2']

WPILIB_VERSION = "2022.3.1"

FRCMAVEN_ARTIFACTS = ['edu.wpi.first.hal:hal-java:2022.3.1', 'edu.wpi.first.wpiutil:wpiutil-java:2022.3.1', 'edu.wpi.first.wpimath:wpimath-java:2022.3.1', 'edu.wpi.first.ntcore:ntcore-java:2022.3.1', 'edu.wpi.first.cscore:cscore-java:2022.3.1', 'edu.wpi.first.cameraserver:cameraserver-java:2022.3.1', 'edu.wpi.first.thirdparty.frc2022.opencv:opencv-java:4.5.2-1']

def library_deps_setup():

    http_file(
        name = "wpilibj_jar_file",
        downloaded_file_path = "wpilibj.jar",  # java_import needs the filename to contain .jar
        sha256 = "4f98ee6970c2ec95b262b8908ed7cd7e7706de5376d3e9ff90ccb9c5ed2f46ac",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpilibj/wpilibj-java/2022.3.1/wpilibj-java-2022.3.1.jar"],
    )

    http_file(
        name = "wpilibj_sources_jar_file",
        downloaded_file_path = "wpilibj_sources.jar",
        sha256 = "8418dd7dde9904e596af7d9e9f2f1d67e7bb5130e4a7ca611e17ea2bb4e860a8",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpilibj/wpilibj-java/2022.3.1/wpilibj-java-2022.3.1-sources.jar"],
    )


    http_archive(
        name = "ni_visa_headers_files",
        build_file_content = """
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
    visibility = ["@//third_party/ni:__pkg__"],
)
""",
        sha256 = "7e428729d299cae5b0e1acd34f38609af6544c83095c3b4b5897d0183eb1a15f",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ni-libraries/visa/2022.2.3/visa-2022.2.3-headers.zip"]
    )

    http_archive(
        name = "ni_visa_athena_files",
        build_file_content = """
filegroup(
    name = "binaries_files",
    srcs = glob([
        "**/*.so*",
        "**/*.dylib",
        "**/*.dll",
    ]),
    visibility = ["@//third_party/ni:__pkg__"],
)

filegroup(
    name = "interface_binaries_files",
    srcs = glob([
        "**/*.lib",
    ]),
    visibility = ["@//third_party/ni:__pkg__"],
)
""",
        sha256 = "014fff5a4684f3c443cbed8c52f19687733dd7053a98a1dad89941801e0b7930",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ni-libraries/visa/2022.2.3/visa-2022.2.3-linuxathena.zip"]
    )

    http_archive(
        name = "ni_netcomm_headers_files",
        build_file_content = """
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
    visibility = ["@//third_party/ni:__pkg__"],
)
""",
        sha256 = "fddb8924887bd46c1d51099327085e821f30c1b2feefd168f1211675c17937d8",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ni-libraries/netcomm/2022.2.3/netcomm-2022.2.3-headers.zip"]
    )

    http_archive(
        name = "ni_netcomm_athena_files",
        build_file_content = """
filegroup(
    name = "binaries_files",
    srcs = glob([
        "**/*.so*",
        "**/*.dylib",
        "**/*.dll",
    ]),
    visibility = ["@//third_party/ni:__pkg__"],
)

filegroup(
    name = "interface_binaries_files",
    srcs = glob([
        "**/*.lib",
    ]),
    visibility = ["@//third_party/ni:__pkg__"],
)
""",
        sha256 = "f56c2fc0943f27f174642664b37fb4529d6f2b6405fe41a639a4c9f31e175c73",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ni-libraries/netcomm/2022.2.3/netcomm-2022.2.3-linuxathena.zip"]
    )

    http_archive(
        name = "ni_chipobject_headers_files",
        build_file_content = """
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
    visibility = ["@//third_party/ni:__pkg__"],
)
""",
        sha256 = "c43e7e9eb7a48c1f97c6b21a0474ec7c8527fbeb3d6c5f62d4d9566771afe25e",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ni-libraries/chipobject/2022.2.3/chipobject-2022.2.3-headers.zip"]
    )

    http_archive(
        name = "ni_chipobject_athena_files",
        build_file_content = """
filegroup(
    name = "binaries_files",
    srcs = glob([
        "**/*.so*",
        "**/*.dylib",
        "**/*.dll",
    ]),
    visibility = ["@//third_party/ni:__pkg__"],
)

filegroup(
    name = "interface_binaries_files",
    srcs = glob([
        "**/*.lib",
    ]),
    visibility = ["@//third_party/ni:__pkg__"],
)
""",
        sha256 = "fdf47ae5ce052edd82ba2b6e007faabf9286f87b079e3789afc3235733d5475c",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ni-libraries/chipobject/2022.2.3/chipobject-2022.2.3-linuxathena.zip"]
    )

    http_archive(
        name = "ni_runtime_athena_files",
        build_file_content = """
filegroup(
    name = "binaries_files",
    srcs = glob([
        "**/*.so*",
        "**/*.dylib",
        "**/*.dll",
    ]),
    visibility = ["@//third_party/ni:__pkg__"],
)

filegroup(
    name = "interface_binaries_files",
    srcs = glob([
        "**/*.lib",
    ]),
    visibility = ["@//third_party/ni:__pkg__"],
)
""",
        sha256 = "186d1b41e96c5d761705221afe0b9f488d1ce86e8149a7b0afdc3abc93097266",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ni-libraries/runtime/2022.2.3/runtime-2022.2.3-linuxathena.zip"]
    )

    http_archive(
        name = "wpilib_wpiutil_headers_files",
        build_file_content = """
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
    visibility = ["@//third_party/wpilib:__pkg__"],
)
""",
        sha256 = "519eaac8a91663b142d811a199fe8abf46a086dbc9f82317887322008473cca1",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpiutil/wpiutil-cpp/2022.3.1/wpiutil-cpp-2022.3.1-headers.zip"]
    )

    http_archive(
        name = "wpilib_wpiutil_athena_files",
        build_file_content = """
filegroup(
    name = "binaries_files",
    srcs = glob([
        "**/*.so*",
        "**/*.dylib",
        "**/*.dll",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)

filegroup(
    name = "interface_binaries_files",
    srcs = glob([
        "**/*.lib",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)
""",
        sha256 = "9cdbe79da2555a6fa6dd0dbc8fd01d534dd51bdce939627bf0e1881566f1aba7",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpiutil/wpiutil-cpp/2022.3.1/wpiutil-cpp-2022.3.1-linuxathena.zip"]
    )

    http_archive(
        name = "wpilib_wpiutil_linux_x64_files",
        build_file_content = """
filegroup(
    name = "binaries_files",
    srcs = glob([
        "**/*.so*",
        "**/*.dylib",
        "**/*.dll",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)

filegroup(
    name = "interface_binaries_files",
    srcs = glob([
        "**/*.lib",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)
""",
        sha256 = "56cc57f4e0252c122a77843ff244025bb39ad36229104e6a9c6a47a51d913bc7",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpiutil/wpiutil-cpp/2022.3.1/wpiutil-cpp-2022.3.1-linuxx86-64.zip"]
    )

    http_archive(
        name = "wpilib_wpiutil_windows_x64_files",
        build_file_content = """
filegroup(
    name = "binaries_files",
    srcs = glob([
        "**/*.so*",
        "**/*.dylib",
        "**/*.dll",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)

filegroup(
    name = "interface_binaries_files",
    srcs = glob([
        "**/*.lib",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)
""",
        sha256 = "a3c99c0ba61c6f215a3efa37249f21b67c60ee3c051a88796eabf3c91edd6a69",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpiutil/wpiutil-cpp/2022.3.1/wpiutil-cpp-2022.3.1-windowsx86-64.zip"]
    )

    http_archive(
        name = "wpilib_wpiutil_macos_x64_files",
        build_file_content = """
filegroup(
    name = "binaries_files",
    srcs = glob([
        "**/*.so*",
        "**/*.dylib",
        "**/*.dll",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)

filegroup(
    name = "interface_binaries_files",
    srcs = glob([
        "**/*.lib",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)
""",
        sha256 = "e5481f51dec5fcf2ee912acfd24bcc6705c3e08e4cd38072dda88188329a7b72",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpiutil/wpiutil-cpp/2022.3.1/wpiutil-cpp-2022.3.1-osxx86-64.zip"]
    )

    http_archive(
        name = "wpilib_wpimath_headers_files",
        build_file_content = """
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
    visibility = ["@//third_party/wpilib:__pkg__"],
)
""",
        sha256 = "1a2165cf8127a2a45cc673517eb0af5e7e92fee377b35e4e83195538a024d65a",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpimath/wpimath-cpp/2022.3.1/wpimath-cpp-2022.3.1-headers.zip"]
    )

    http_archive(
        name = "wpilib_wpimath_athena_files",
        build_file_content = """
filegroup(
    name = "binaries_files",
    srcs = glob([
        "**/*.so*",
        "**/*.dylib",
        "**/*.dll",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)

filegroup(
    name = "interface_binaries_files",
    srcs = glob([
        "**/*.lib",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)
""",
        sha256 = "316fac1af3cc3932d7364e487f109d31abc37cc2ed950e49d598f044d87d16d0",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpimath/wpimath-cpp/2022.3.1/wpimath-cpp-2022.3.1-linuxathena.zip"]
    )

    http_archive(
        name = "wpilib_wpimath_linux_x64_files",
        build_file_content = """
filegroup(
    name = "binaries_files",
    srcs = glob([
        "**/*.so*",
        "**/*.dylib",
        "**/*.dll",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)

filegroup(
    name = "interface_binaries_files",
    srcs = glob([
        "**/*.lib",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)
""",
        sha256 = "aff716f9a6b5d830fe3c90a1ea0e5e8e4ad86fbfc2f4f8d7f9090e1517685c9c",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpimath/wpimath-cpp/2022.3.1/wpimath-cpp-2022.3.1-linuxx86-64.zip"]
    )

    http_archive(
        name = "wpilib_wpimath_windows_x64_files",
        build_file_content = """
filegroup(
    name = "binaries_files",
    srcs = glob([
        "**/*.so*",
        "**/*.dylib",
        "**/*.dll",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)

filegroup(
    name = "interface_binaries_files",
    srcs = glob([
        "**/*.lib",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)
""",
        sha256 = "aa166dabcfdd92e89ffce0aab62b8898d11f8072c456892627a14f18ffed8f73",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpimath/wpimath-cpp/2022.3.1/wpimath-cpp-2022.3.1-windowsx86-64.zip"]
    )

    http_archive(
        name = "wpilib_wpimath_macos_x64_files",
        build_file_content = """
filegroup(
    name = "binaries_files",
    srcs = glob([
        "**/*.so*",
        "**/*.dylib",
        "**/*.dll",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)

filegroup(
    name = "interface_binaries_files",
    srcs = glob([
        "**/*.lib",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)
""",
        sha256 = "2dd218696838eb57999a0ce0d8ae4c6969d37a48bbdc0fa26dc8e60fbee6783b",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpimath/wpimath-cpp/2022.3.1/wpimath-cpp-2022.3.1-osxx86-64.zip"]
    )

    http_archive(
        name = "wpilib_ntcore_headers_files",
        build_file_content = """
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
    visibility = ["@//third_party/wpilib:__pkg__"],
)
""",
        sha256 = "144431c4aeb28d9fd4ddd44614ca7b6f9fc0b87b8488394175b66d76979beb83",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ntcore/ntcore-cpp/2022.3.1/ntcore-cpp-2022.3.1-headers.zip"]
    )

    http_archive(
        name = "wpilib_ntcore_athena_files",
        build_file_content = """
filegroup(
    name = "binaries_files",
    srcs = glob([
        "**/*.so*",
        "**/*.dylib",
        "**/*.dll",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)

filegroup(
    name = "interface_binaries_files",
    srcs = glob([
        "**/*.lib",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)
""",
        sha256 = "e2330da4c4a424416fb4f1cfd670ca55aa86e4ea1f397718f0fcaf013230fc4b",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ntcore/ntcore-cpp/2022.3.1/ntcore-cpp-2022.3.1-linuxathena.zip"]
    )

    http_archive(
        name = "wpilib_ntcore_linux_x64_files",
        build_file_content = """
filegroup(
    name = "binaries_files",
    srcs = glob([
        "**/*.so*",
        "**/*.dylib",
        "**/*.dll",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)

filegroup(
    name = "interface_binaries_files",
    srcs = glob([
        "**/*.lib",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)
""",
        sha256 = "af7de76b24702c30504ccd6d92d2265eb5f1402cbf08b418217e8643adca78b5",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ntcore/ntcore-cpp/2022.3.1/ntcore-cpp-2022.3.1-linuxx86-64.zip"]
    )

    http_archive(
        name = "wpilib_ntcore_windows_x64_files",
        build_file_content = """
filegroup(
    name = "binaries_files",
    srcs = glob([
        "**/*.so*",
        "**/*.dylib",
        "**/*.dll",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)

filegroup(
    name = "interface_binaries_files",
    srcs = glob([
        "**/*.lib",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)
""",
        sha256 = "60f5aa44a9dbc4465302a3086f4932a48e2e6cf6f06d9e544182395c95982c1b",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ntcore/ntcore-cpp/2022.3.1/ntcore-cpp-2022.3.1-windowsx86-64.zip"]
    )

    http_archive(
        name = "wpilib_ntcore_macos_x64_files",
        build_file_content = """
filegroup(
    name = "binaries_files",
    srcs = glob([
        "**/*.so*",
        "**/*.dylib",
        "**/*.dll",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)

filegroup(
    name = "interface_binaries_files",
    srcs = glob([
        "**/*.lib",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)
""",
        sha256 = "2d7eaf19f375c98cc37f5f6efd8ea428fd142f4e422233a22db60f452b15a746",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ntcore/ntcore-cpp/2022.3.1/ntcore-cpp-2022.3.1-osxx86-64.zip"]
    )

    http_archive(
        name = "wpilib_hal_headers_files",
        build_file_content = """
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
    visibility = ["@//third_party/wpilib:__pkg__"],
)
""",
        sha256 = "30538817968f6f905008fa3e4b9cb034ab7a896307fedd9568b0df5412941c2d",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/hal/hal-cpp/2022.3.1/hal-cpp-2022.3.1-headers.zip"]
    )

    http_archive(
        name = "wpilib_hal_athena_files",
        build_file_content = """
filegroup(
    name = "binaries_files",
    srcs = glob([
        "**/*.so*",
        "**/*.dylib",
        "**/*.dll",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)

filegroup(
    name = "interface_binaries_files",
    srcs = glob([
        "**/*.lib",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)
""",
        sha256 = "d0cd66dd6d1f15e4ed98dd45cb9d1145f18a627937245bf2a2695ffa5ee21c32",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/hal/hal-cpp/2022.3.1/hal-cpp-2022.3.1-linuxathena.zip"]
    )

    http_archive(
        name = "wpilib_hal_linux_x64_files",
        build_file_content = """
filegroup(
    name = "binaries_files",
    srcs = glob([
        "**/*.so*",
        "**/*.dylib",
        "**/*.dll",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)

filegroup(
    name = "interface_binaries_files",
    srcs = glob([
        "**/*.lib",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)
""",
        sha256 = "11b477e7e8f398265e9a5cec16f8fdf85dd9ac96aedd4ea43b5088f496061a01",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/hal/hal-cpp/2022.3.1/hal-cpp-2022.3.1-linuxx86-64.zip"]
    )

    http_archive(
        name = "wpilib_hal_windows_x64_files",
        build_file_content = """
filegroup(
    name = "binaries_files",
    srcs = glob([
        "**/*.so*",
        "**/*.dylib",
        "**/*.dll",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)

filegroup(
    name = "interface_binaries_files",
    srcs = glob([
        "**/*.lib",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)
""",
        sha256 = "2871c88c0d7ebc6d13005d698d641f23653d6de446fb630815470d38adab17b5",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/hal/hal-cpp/2022.3.1/hal-cpp-2022.3.1-windowsx86-64.zip"]
    )

    http_archive(
        name = "wpilib_hal_macos_x64_files",
        build_file_content = """
filegroup(
    name = "binaries_files",
    srcs = glob([
        "**/*.so*",
        "**/*.dylib",
        "**/*.dll",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)

filegroup(
    name = "interface_binaries_files",
    srcs = glob([
        "**/*.lib",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)
""",
        sha256 = "085445d4df4b17f2284dfac64ed57e1a2de5be9be4ef517f016a75663de6b2de",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/hal/hal-cpp/2022.3.1/hal-cpp-2022.3.1-osxx86-64.zip"]
    )

    http_archive(
        name = "wpilib_halsim_gui_linux_x64_files",
        build_file_content = """
filegroup(
    name = "binaries_files",
    srcs = glob([
        "**/*.so*",
        "**/*.dylib",
        "**/*.dll",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)

filegroup(
    name = "interface_binaries_files",
    srcs = glob([
        "**/*.lib",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)
""",
        sha256 = "182d81197bde187737131802e9df0e4aa32eac6a925e8d9fbddbc29fe107fc3e",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/halsim/halsim_gui/2022.3.1/halsim_gui-2022.3.1-linuxx86-64.zip"]
    )

    http_archive(
        name = "wpilib_halsim_gui_windows_x64_files",
        build_file_content = """
filegroup(
    name = "binaries_files",
    srcs = glob([
        "**/*.so*",
        "**/*.dylib",
        "**/*.dll",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)

filegroup(
    name = "interface_binaries_files",
    srcs = glob([
        "**/*.lib",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)
""",
        sha256 = "4ff4c97108467f8d08986f6cf64bbd82b4a117e367d04394dcf18286ae95fd80",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/halsim/halsim_gui/2022.3.1/halsim_gui-2022.3.1-windowsx86-64.zip"]
    )

    http_archive(
        name = "wpilib_halsim_gui_macos_x64_files",
        build_file_content = """
filegroup(
    name = "binaries_files",
    srcs = glob([
        "**/*.so*",
        "**/*.dylib",
        "**/*.dll",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)

filegroup(
    name = "interface_binaries_files",
    srcs = glob([
        "**/*.lib",
    ]),
    visibility = ["@//third_party/wpilib:__pkg__"],
)
""",
        sha256 = "3dc1ea448b05985882f4bf356d8ba8f34abded52303c62312ee39235e1c9fd5d",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/halsim/halsim_gui/2022.3.1/halsim_gui-2022.3.1-osxx86-64.zip"]
    )

