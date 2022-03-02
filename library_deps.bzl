
# THIS FILE IS GENERATED.  DO NOT MODIFY (except to run the formatter).
# To modify the contents of this file, change the constants near the top of generate_library_deps.py, and then run that script with python3.

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive", "http_file")

MAVEN_ARTIFACTS = ['io.github.classgraph:classgraph:4.8.128', 'com.fasterxml.jackson.core:jackson-annotations:2.10.0', 'com.fasterxml.jackson.core:jackson-core:2.10.0', 'com.fasterxml.jackson.core:jackson-databind:2.10.0', 'org.ejml:ejml-simple:0.38', 'org.ejml:ejml-core:0.38', 'junit:junit:4.13.2']

WPILIB_VERSION = "2022.4.1"

FRCMAVEN_ARTIFACTS = ['edu.wpi.first.hal:hal-java:2022.4.1', 'edu.wpi.first.wpiutil:wpiutil-java:2022.4.1', 'edu.wpi.first.wpimath:wpimath-java:2022.4.1', 'edu.wpi.first.ntcore:ntcore-java:2022.4.1', 'edu.wpi.first.cscore:cscore-java:2022.4.1', 'edu.wpi.first.cameraserver:cameraserver-java:2022.4.1', 'edu.wpi.first.thirdparty.frc2022.opencv:opencv-java:4.5.2-1']

def library_deps_setup():

    http_file(
        name = "wpilibj_jar_file",
        downloaded_file_path = "wpilibj.jar",  # java_import needs the filename to contain .jar
        sha256 = "298fec2cf559cdfc429443220647379b6cb0cc2c5c9350101df700eae14f107c",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpilibj/wpilibj-java/2022.4.1/wpilibj-java-2022.4.1.jar"],
    )

    http_file(
        name = "wpilibj_sources_jar_file",
        downloaded_file_path = "wpilibj_sources.jar",
        sha256 = "4175a7f0f8e4df6132d2dcb146acca15633b611b36eb6e0581260834f9636bfe",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpilibj/wpilibj-java/2022.4.1/wpilibj-java-2022.4.1-sources.jar"],
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
        sha256 = "942259f211f59ec6e8e72ef5b1991ef4d4084c6cd7bcf52b17d5a22e5377340a",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpiutil/wpiutil-cpp/2022.4.1/wpiutil-cpp-2022.4.1-headers.zip"]
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
        sha256 = "185ed776a5a8de78d817a39a3c6da313623624d5c8beff25e48971efb9705483",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpiutil/wpiutil-cpp/2022.4.1/wpiutil-cpp-2022.4.1-linuxathena.zip"]
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
        sha256 = "17db0ec3133fea1588d90f8f0c8c5a778cc1d8a16c0c667f6a9b0ebc6310cb82",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpiutil/wpiutil-cpp/2022.4.1/wpiutil-cpp-2022.4.1-linuxx86-64.zip"]
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
        sha256 = "b7aad4d4562a86a89106f9c28043ed78c785fe8c4715fc9de677eebffa232263",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpiutil/wpiutil-cpp/2022.4.1/wpiutil-cpp-2022.4.1-windowsx86-64.zip"]
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
        sha256 = "82e3d024065b9d3c875d99f1b5e1c0ae2b92a1dca63ff3f514c9015cf2939b1c",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpiutil/wpiutil-cpp/2022.4.1/wpiutil-cpp-2022.4.1-osxx86-64.zip"]
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
        sha256 = "ba1baa5d7d2b206ca7f385d05e50347ae33cbfb226d33650b3a8b5cdadee2361",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpimath/wpimath-cpp/2022.4.1/wpimath-cpp-2022.4.1-headers.zip"]
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
        sha256 = "3e5b0dd54681e843f016a7a75d1774784f37163ef21c95b0840389a6c8de16d5",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpimath/wpimath-cpp/2022.4.1/wpimath-cpp-2022.4.1-linuxathena.zip"]
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
        sha256 = "80bb40c420fe3da5aa8c1e07fcae199e4cef97d5903dcef66d5847233a190c63",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpimath/wpimath-cpp/2022.4.1/wpimath-cpp-2022.4.1-linuxx86-64.zip"]
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
        sha256 = "66989de51f3624572a3e6edaff9bbcf980076f5d3312022b1e19b5977b7f61f3",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpimath/wpimath-cpp/2022.4.1/wpimath-cpp-2022.4.1-windowsx86-64.zip"]
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
        sha256 = "c37e0735b12aaab1a95cd5906ad8eeeb2320caffb486b57181585fc8176b46aa",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpimath/wpimath-cpp/2022.4.1/wpimath-cpp-2022.4.1-osxx86-64.zip"]
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
        sha256 = "8f2327f191227d0bffdf0d41c7362333ede63a97b751c62b06c0ba243fa9ac2a",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ntcore/ntcore-cpp/2022.4.1/ntcore-cpp-2022.4.1-headers.zip"]
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
        sha256 = "f8ca79badc438c20ed6d690b5a6c4c4e2d1a2c053c2b2e98ffa9dc0da0b4c9c7",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ntcore/ntcore-cpp/2022.4.1/ntcore-cpp-2022.4.1-linuxathena.zip"]
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
        sha256 = "b8d59db14bcc353b0f2e48407d4f54d6f7c8949c268a4dbed7ac7222b1b261a8",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ntcore/ntcore-cpp/2022.4.1/ntcore-cpp-2022.4.1-linuxx86-64.zip"]
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
        sha256 = "90753f1c38c0d6eef1654d76d47cf582f19151ff7d16fb77383261b74b3d1adb",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ntcore/ntcore-cpp/2022.4.1/ntcore-cpp-2022.4.1-windowsx86-64.zip"]
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
        sha256 = "545920724653507d075ef1314108d0479d4e863c08f9f72b2b68974c701e9ddd",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ntcore/ntcore-cpp/2022.4.1/ntcore-cpp-2022.4.1-osxx86-64.zip"]
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
        sha256 = "c06dfa93a47d2c400f0686c3adaf902e9dd0d7ee810e3406886e07e83ad8a238",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/hal/hal-cpp/2022.4.1/hal-cpp-2022.4.1-headers.zip"]
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
        sha256 = "dbc000d7305841e5ab09d86b5f2d2cfd5dcd07ad383809eaf3444c02367c93f6",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/hal/hal-cpp/2022.4.1/hal-cpp-2022.4.1-linuxathena.zip"]
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
        sha256 = "5d50d46b563b554feb10a6a7a54cce68172008a7a336874d8332ae390e4bbbb5",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/hal/hal-cpp/2022.4.1/hal-cpp-2022.4.1-linuxx86-64.zip"]
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
        sha256 = "d8a6b469308e63c79df42dfb49eff8aa65530bc99b5e748e955a35be5e9e104e",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/hal/hal-cpp/2022.4.1/hal-cpp-2022.4.1-windowsx86-64.zip"]
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
        sha256 = "aef7e14f9e18eb524b747cae10934363b46086e9c4d42d945a132f09d5c6baa8",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/hal/hal-cpp/2022.4.1/hal-cpp-2022.4.1-osxx86-64.zip"]
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
        sha256 = "21ce18897aaaf3fafc12581598025073e5034378ddf55cdb094f9d0dfcfccf7e",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/halsim/halsim_gui/2022.4.1/halsim_gui-2022.4.1-linuxx86-64.zip"]
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
        sha256 = "bd998732d7c48f2f4a519da107f8a98dfff46e40e18fa6a278e508cc6f8ff803",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/halsim/halsim_gui/2022.4.1/halsim_gui-2022.4.1-windowsx86-64.zip"]
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
        sha256 = "796d32875d73cee8830e7451f23230c9c4763d3690e62a1329955d88de388c57",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/halsim/halsim_gui/2022.4.1/halsim_gui-2022.4.1-osxx86-64.zip"]
    )

