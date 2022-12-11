
# THIS FILE IS GENERATED.  DO NOT MODIFY (except to run the formatter).
# To modify the contents of this file, change the constants near the top of generate_library_deps.py, and then run that script with python3.

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive", "http_file")

MAVEN_ARTIFACTS = ['io.github.classgraph:classgraph:4.8.128', 'com.fasterxml.jackson.core:jackson-annotations:2.10.0', 'com.fasterxml.jackson.core:jackson-core:2.10.0', 'com.fasterxml.jackson.core:jackson-databind:2.10.0', 'org.ejml:ejml-simple:0.38', 'org.ejml:ejml-core:0.38', 'junit:junit:4.13.2', 'com.squareup:javapoet:1.13.0']

WPILIB_VERSION = "2023.1.1-beta-6"

FRCMAVEN_ARTIFACTS = ['edu.wpi.first.hal:hal-java:2023.1.1-beta-6', 'edu.wpi.first.wpiutil:wpiutil-java:2023.1.1-beta-6', 'edu.wpi.first.wpimath:wpimath-java:2023.1.1-beta-6', 'edu.wpi.first.ntcore:ntcore-java:2023.1.1-beta-6', 'edu.wpi.first.cscore:cscore-java:2023.1.1-beta-6', 'edu.wpi.first.cameraserver:cameraserver-java:2023.1.1-beta-6', 'edu.wpi.first.thirdparty.frc2023.opencv:opencv-java:4.6.0-2']

def library_deps_setup():

    http_file(
        name = "wpilibj_jar_file",
        downloaded_file_path = "wpilibj.jar",  # java_import needs the filename to contain .jar
        sha256 = "643babc040415dd470c24790d3d97b4e88242b6ef029373afe12979d7d4cfe8a",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpilibj/wpilibj-java/2023.1.1-beta-6/wpilibj-java-2023.1.1-beta-6.jar"],
    )

    http_file(
        name = "wpilibj_sources_jar_file",
        downloaded_file_path = "wpilibj_sources.jar",
        sha256 = "d1e0a4ff393fdfa57e10d02c9c0bb494f5cc68695951beecc951de88974951d9",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpilibj/wpilibj-java/2023.1.1-beta-6/wpilibj-java-2023.1.1-beta-6-sources.jar"],
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
        sha256 = "cafce12b1e58c6966028ba25652d2e703958c08d46a2c87804a30422bd3455f0",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ni-libraries/visa/2023.1.0/visa-2023.1.0-headers.zip"]
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
        sha256 = "3dd30a3ad210d929fe96f02a34cca9f378abae0dc5181e20c9a184d212e86bcd",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ni-libraries/visa/2023.1.0/visa-2023.1.0-linuxathena.zip"]
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
        sha256 = "3770998273495ab6195b40913fbd8fe6f7cadc64fb994314b4e29841960f65fa",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ni-libraries/netcomm/2023.1.0/netcomm-2023.1.0-headers.zip"]
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
        sha256 = "e5d31faa216fe9db3cf88ca8ace0fa6c969d0cef00d59dd7a8cad1da29e045ed",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ni-libraries/netcomm/2023.1.0/netcomm-2023.1.0-linuxathena.zip"]
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
        sha256 = "61cb5991ff859c93bf8fcd21b0c97ad5603ff4de5ef5ecfb45b2598da2d3d068",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ni-libraries/chipobject/2023.1.0/chipobject-2023.1.0-headers.zip"]
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
        sha256 = "6844c487f325075c532d342f4acc0260121e80515d2b652b97395e7df8f6ea65",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ni-libraries/chipobject/2023.1.0/chipobject-2023.1.0-linuxathena.zip"]
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
        sha256 = "12ed2b14933877047cc20e26b20160d4961c668a1e1308f88b1294b007cd83d5",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ni-libraries/runtime/2023.1.0/runtime-2023.1.0-linuxathena.zip"]
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
        sha256 = "01834892965cdf3d13cb18b693e35d9a0eb58ccdeb1604cbb8c890043a9f0234",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpiutil/wpiutil-cpp/2023.1.1-beta-6/wpiutil-cpp-2023.1.1-beta-6-headers.zip"]
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
        sha256 = "2b154f0fd7936f8b4d08f71a6ed1df8decdcfde5d5b58c7873437ffe654dc1a9",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpiutil/wpiutil-cpp/2023.1.1-beta-6/wpiutil-cpp-2023.1.1-beta-6-linuxathena.zip"]
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
        sha256 = "ddf0c0f9cd136c7d782e2de01fa2afc47988da4288b7cd2859b26a410c78c7b6",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpiutil/wpiutil-cpp/2023.1.1-beta-6/wpiutil-cpp-2023.1.1-beta-6-linuxx86-64.zip"]
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
        sha256 = "9cc29f7b620b4fd10978ca00f035a6f3efcb8f163c25b9bd7cfd3531bc09c328",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpiutil/wpiutil-cpp/2023.1.1-beta-6/wpiutil-cpp-2023.1.1-beta-6-windowsx86-64.zip"]
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
        sha256 = "f1d00604039cfcb0e5c7491f9a4a9965a730975fc819618ac3dd9f39abf569fd",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpiutil/wpiutil-cpp/2023.1.1-beta-6/wpiutil-cpp-2023.1.1-beta-6-osxuniversal.zip"]
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
        sha256 = "b1e24876f8b384b7aeb218b885fc5ebea57c69b8f4691ea61b3dcc1ac1457f4a",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpimath/wpimath-cpp/2023.1.1-beta-6/wpimath-cpp-2023.1.1-beta-6-headers.zip"]
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
        sha256 = "d6d246ec213e7071ff6d66d9fda7923b03fabe2a9d91b3783337f0fa41d36bf2",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpimath/wpimath-cpp/2023.1.1-beta-6/wpimath-cpp-2023.1.1-beta-6-linuxathena.zip"]
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
        sha256 = "ec2348c39f833f28d362f16d780e7a8ed234000d711076f2c6b372322ab13376",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpimath/wpimath-cpp/2023.1.1-beta-6/wpimath-cpp-2023.1.1-beta-6-linuxx86-64.zip"]
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
        sha256 = "312ba89ac1ad13a3b9993a16f508c63e1fdc83929d4805fb83329f9d9740de4d",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpimath/wpimath-cpp/2023.1.1-beta-6/wpimath-cpp-2023.1.1-beta-6-windowsx86-64.zip"]
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
        sha256 = "e62db63d3383576f6c7c45139657fbc03e13382bc7cee54e9bc57cff21fb1092",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpimath/wpimath-cpp/2023.1.1-beta-6/wpimath-cpp-2023.1.1-beta-6-osxuniversal.zip"]
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
        sha256 = "4f04b972105b8cb556c08b0a639e37f3b3b0c97aa7bf48094366a226edb1d774",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ntcore/ntcore-cpp/2023.1.1-beta-6/ntcore-cpp-2023.1.1-beta-6-headers.zip"]
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
        sha256 = "c945d226d2d0be0a6fc7f05c2edd0558e8f7e3d32887a6743d89947c9c5bfa39",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ntcore/ntcore-cpp/2023.1.1-beta-6/ntcore-cpp-2023.1.1-beta-6-linuxathena.zip"]
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
        sha256 = "225ebd324fddbe8f7e06bd3637a8777e61d1dbcaf03e0737d21aa726e56f5d8f",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ntcore/ntcore-cpp/2023.1.1-beta-6/ntcore-cpp-2023.1.1-beta-6-linuxx86-64.zip"]
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
        sha256 = "4c116e68703c01e2888d5608ec7588add05fbc4120f0ea5aa469c90807c2e3e4",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ntcore/ntcore-cpp/2023.1.1-beta-6/ntcore-cpp-2023.1.1-beta-6-windowsx86-64.zip"]
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
        sha256 = "4b44ae683d5ec192480d0560b57897052e33339d1e7bbaef55a44232e05ead3c",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ntcore/ntcore-cpp/2023.1.1-beta-6/ntcore-cpp-2023.1.1-beta-6-osxuniversal.zip"]
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
        sha256 = "f1a0fe343cd7628b8d58e5a2df71f733ae19e33835b9746bd31455a58a86d835",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/hal/hal-cpp/2023.1.1-beta-6/hal-cpp-2023.1.1-beta-6-headers.zip"]
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
        sha256 = "0482bd8c0b1ae5e08ad2e6fedefdcb40e6d5c7f198b8d1fabb89d92f96fcb275",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/hal/hal-cpp/2023.1.1-beta-6/hal-cpp-2023.1.1-beta-6-linuxathena.zip"]
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
        sha256 = "6bdff0b36f584aee6a68cbb59c25a3f0100163ae2efdb9dd89e606490b236927",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/hal/hal-cpp/2023.1.1-beta-6/hal-cpp-2023.1.1-beta-6-linuxx86-64.zip"]
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
        sha256 = "cb9f6ad5aa53117733abe609044781d5d18c236e3170996770abc80b1a5952fc",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/hal/hal-cpp/2023.1.1-beta-6/hal-cpp-2023.1.1-beta-6-windowsx86-64.zip"]
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
        sha256 = "3ce515b5ccd3560daf4b7b09a441919e5405f1f2f875a8d7d9d9329b101bcdd7",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/hal/hal-cpp/2023.1.1-beta-6/hal-cpp-2023.1.1-beta-6-osxuniversal.zip"]
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
        sha256 = "10c9921555d85f0abc5c683b6bced1b2f6953d9220af310e148aa7357dac483f",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/halsim/halsim_gui/2023.1.1-beta-6/halsim_gui-2023.1.1-beta-6-linuxx86-64.zip"]
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
        sha256 = "3ad8baddd779b9b54af843caa421bce4207f31eec16566daeee21581986ab106",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/halsim/halsim_gui/2023.1.1-beta-6/halsim_gui-2023.1.1-beta-6-windowsx86-64.zip"]
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
        sha256 = "d39467167e501cd314e1036597712ea93b6ea7ff9c984d609422d8a170de23e7",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/halsim/halsim_gui/2023.1.1-beta-6/halsim_gui-2023.1.1-beta-6-osxuniversal.zip"]
    )

