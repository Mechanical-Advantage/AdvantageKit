
# THIS FILE IS GENERATED.  DO NOT MODIFY (except to run the formatter).
# To modify the contents of this file, change the constants near the top of generate_library_deps.py, and then run that script with python3.

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive", "http_file")

MAVEN_ARTIFACTS = ['io.github.classgraph:classgraph:4.8.128', 'com.fasterxml.jackson.core:jackson-annotations:2.15.2', 'com.fasterxml.jackson.core:jackson-core:2.15.2', 'com.fasterxml.jackson.core:jackson-databind:2.15.2', 'org.ejml:ejml-simple:0.43.1', 'org.ejml:ejml-core:0.43.1', 'junit:junit:4.13.2', 'com.squareup:javapoet:1.13.0', 'us.hebi.quickbuf:quickbuf-runtime:1.3.2']

WPILIB_VERSION = "2024.1.1"

FRCMAVEN_ARTIFACTS = ['edu.wpi.first.hal:hal-java:2024.1.1', 'edu.wpi.first.wpiutil:wpiutil-java:2024.1.1', 'edu.wpi.first.wpimath:wpimath-java:2024.1.1', 'edu.wpi.first.ntcore:ntcore-java:2024.1.1', 'edu.wpi.first.cscore:cscore-java:2024.1.1', 'edu.wpi.first.cameraserver:cameraserver-java:2024.1.1', 'edu.wpi.first.wpiunits:wpiunits-java:2024.1.1', 'edu.wpi.first.thirdparty.frc2024.opencv:opencv-java:4.8.0-2']

def library_deps_setup():

    http_file(
        name = "wpilibj_jar_file",
        downloaded_file_path = "wpilibj.jar",  # java_import needs the filename to contain .jar
        sha256 = "d88f13d6b428381e34d32efa4ccdcdc73afa0b7d0d455d11bfd5118ea7b48bd1",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpilibj/wpilibj-java/2024.1.1/wpilibj-java-2024.1.1.jar"],
    )

    http_file(
        name = "wpilibj_sources_jar_file",
        downloaded_file_path = "wpilibj_sources.jar",
        sha256 = "86bbeef0c0378dde34fcf70fc294a948616c69828ffffe8ce7daa9301216bfca",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpilibj/wpilibj-java/2024.1.1/wpilibj-java-2024.1.1-sources.jar"],
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
        sha256 = "68fb8880e37863cddb39c1514ac73f3c5a4b5c79961c356f7d9e60dc02cddf15",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ni-libraries/visa/2024.2.1/visa-2024.2.1-headers.zip"]
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
        sha256 = "162511fd08957eb9a35c2b97ec8584fc5b43c6b9a8deb049de88376690a495fe",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ni-libraries/visa/2024.2.1/visa-2024.2.1-linuxathena.zip"]
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
        sha256 = "b3de04c3e8950c9017ec1a6adce7ca6be295aa1a599a931778f96ab1b4245786",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ni-libraries/netcomm/2024.2.1/netcomm-2024.2.1-headers.zip"]
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
        sha256 = "8b1ba57c0fde3b1b3216a16bdf31b9440f3b7782637384cdca34e217e0eb4bf9",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ni-libraries/netcomm/2024.2.1/netcomm-2024.2.1-linuxathena.zip"]
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
        sha256 = "c4299902974440b00d9046d4473d9350cb9489cd673f0875eadf88441056461f",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ni-libraries/chipobject/2024.2.1/chipobject-2024.2.1-headers.zip"]
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
        sha256 = "c42aee14154143489825022c76aaeeeefe46840c89ad48c757f7c70f8a46cf7f",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ni-libraries/chipobject/2024.2.1/chipobject-2024.2.1-linuxathena.zip"]
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
        sha256 = "5ad951daea54da1193ebd4416a8077ee3de9abd8c8974cc7275238c1221687b2",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ni-libraries/runtime/2024.2.1/runtime-2024.2.1-linuxathena.zip"]
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
        sha256 = "ba30d7e5415188a22c6821935efc8d9a2d52c5889649eac7f084c991f033e81a",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpiutil/wpiutil-cpp/2024.1.1/wpiutil-cpp-2024.1.1-headers.zip"]
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
        sha256 = "9dbaa28ec589fd52b237fd0bb1897dc8f5cc2bf802c742938d1370ac2bb6a669",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpiutil/wpiutil-cpp/2024.1.1/wpiutil-cpp-2024.1.1-linuxathena.zip"]
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
        sha256 = "179c21641cac0f8ed5c0de674086392a73c9f823dae52e893a1504869aef9119",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpiutil/wpiutil-cpp/2024.1.1/wpiutil-cpp-2024.1.1-linuxx86-64.zip"]
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
        sha256 = "1277fe0f6ce92b06b60412ec4af5cec6b90b8acaffc353cc83c0ca3926060a1c",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpiutil/wpiutil-cpp/2024.1.1/wpiutil-cpp-2024.1.1-windowsx86-64.zip"]
    )

    http_archive(
        name = "wpilib_wpiutil_macos_universal_files",
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
        sha256 = "5c92f6f7e772ab3d51993de1dcd24721231eff9fc5dabc82a4475124eae3c8e3",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpiutil/wpiutil-cpp/2024.1.1/wpiutil-cpp-2024.1.1-osxuniversal.zip"]
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
        sha256 = "827c82f474a56d9dca6c3983b70027449d751a03b32f7d87160e2e0c48f1e70d",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpimath/wpimath-cpp/2024.1.1/wpimath-cpp-2024.1.1-headers.zip"]
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
        sha256 = "9f73968b0cdc3137cf070cea6a2089f4112b22512a2b3ced7927fa573726f320",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpimath/wpimath-cpp/2024.1.1/wpimath-cpp-2024.1.1-linuxathena.zip"]
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
        sha256 = "666d8db9bf44830590b09ebc7987d76aeaf7971a7bd5d23cf0d388c4975d4e82",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpimath/wpimath-cpp/2024.1.1/wpimath-cpp-2024.1.1-linuxx86-64.zip"]
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
        sha256 = "315d2ac8a5143d92d774b120a8489c3f8ecdcca4adb18e94320cc6c4c70f5a02",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpimath/wpimath-cpp/2024.1.1/wpimath-cpp-2024.1.1-windowsx86-64.zip"]
    )

    http_archive(
        name = "wpilib_wpimath_macos_universal_files",
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
        sha256 = "1f9471f163cec19a36ab095fa43f29bd149a671f9041ffc4bd202164096c4def",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpimath/wpimath-cpp/2024.1.1/wpimath-cpp-2024.1.1-osxuniversal.zip"]
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
        sha256 = "46403e8d398efbd335595bfcb04747d7366667acd07782dca3db3389fcbe7dd9",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ntcore/ntcore-cpp/2024.1.1/ntcore-cpp-2024.1.1-headers.zip"]
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
        sha256 = "148fbc01b0f3adef25a8e70ce4634c8c3c9fca679512770c64d12d9584b0e29e",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ntcore/ntcore-cpp/2024.1.1/ntcore-cpp-2024.1.1-linuxathena.zip"]
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
        sha256 = "8e17c1e52bd4156c5efbbff3d5f4fccbd2aab84514844fbd78ed9a536eddf2cf",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ntcore/ntcore-cpp/2024.1.1/ntcore-cpp-2024.1.1-linuxx86-64.zip"]
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
        sha256 = "072f3c374fd662a0fa00096e841ed5d340d75334ffb7a6ca76d96b20784918db",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ntcore/ntcore-cpp/2024.1.1/ntcore-cpp-2024.1.1-windowsx86-64.zip"]
    )

    http_archive(
        name = "wpilib_ntcore_macos_universal_files",
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
        sha256 = "a4b0298f7da726868e494a63e9f93900900b827d6102abc0668d3416b0c439c8",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ntcore/ntcore-cpp/2024.1.1/ntcore-cpp-2024.1.1-osxuniversal.zip"]
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
        sha256 = "a86464e552397f7b41fce4185d40d4daed425fc88dc229622422cbafd6f6d57b",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/hal/hal-cpp/2024.1.1/hal-cpp-2024.1.1-headers.zip"]
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
        sha256 = "2cd6d2fb9afc091af032cc7ec1e637ed93cdfda49e846a13d6475bed8ff30fa3",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/hal/hal-cpp/2024.1.1/hal-cpp-2024.1.1-linuxathena.zip"]
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
        sha256 = "4762d1b36de153f426d25d037a55b36afacd32494c044485ed7f6efe2b741fb3",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/hal/hal-cpp/2024.1.1/hal-cpp-2024.1.1-linuxx86-64.zip"]
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
        sha256 = "ca032f0663b3e2da146fcacad60a443f3c50613d1179de6456f4ca8b1e553707",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/hal/hal-cpp/2024.1.1/hal-cpp-2024.1.1-windowsx86-64.zip"]
    )

    http_archive(
        name = "wpilib_hal_macos_universal_files",
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
        sha256 = "f8cc2ae89dbe0ddc838c960ab13062bb4eba39a664f48e6cfc7cefb57c9d848c",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/hal/hal-cpp/2024.1.1/hal-cpp-2024.1.1-osxuniversal.zip"]
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
        sha256 = "bffaccd77c2a57773fddbc243c1ee0867041459b087dd7f14e0264e1b0740f63",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/halsim/halsim_gui/2024.1.1/halsim_gui-2024.1.1-linuxx86-64.zip"]
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
        sha256 = "38a52057d6d3380f00f2ef6c44ed7482e6845653e61c42785614edc9c2a7a30d",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/halsim/halsim_gui/2024.1.1/halsim_gui-2024.1.1-windowsx86-64.zip"]
    )

    http_archive(
        name = "wpilib_halsim_gui_macos_universal_files",
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
        sha256 = "cfe7e5cd93143bd58e4a5163e869a6ad019de90e83b4f3e8d31913e2e40e1b8c",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/halsim/halsim_gui/2024.1.1/halsim_gui-2024.1.1-osxuniversal.zip"]
    )

