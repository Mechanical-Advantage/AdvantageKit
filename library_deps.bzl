
# THIS FILE IS GENERATED.  DO NOT MODIFY (except to run the formatter).
# To modify the contents of this file, change the constants near the top of generate_library_deps.py, and then run that script with python3.

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive", "http_file")

MAVEN_ARTIFACTS = ['io.github.classgraph:classgraph:4.8.128', 'com.fasterxml.jackson.core:jackson-annotations:2.15.2', 'com.fasterxml.jackson.core:jackson-core:2.15.2', 'com.fasterxml.jackson.core:jackson-databind:2.15.2', 'org.ejml:ejml-simple:0.43.1', 'org.ejml:ejml-core:0.43.1', 'junit:junit:4.13.2', 'com.squareup:javapoet:1.13.0', 'us.hebi.quickbuf:quickbuf-runtime:1.3.2']

WPILIB_VERSION = "2025.1.1-beta-1"

FRCMAVEN_ARTIFACTS = ['edu.wpi.first.hal:hal-java:2025.1.1-beta-1', 'edu.wpi.first.wpiutil:wpiutil-java:2025.1.1-beta-1', 'edu.wpi.first.wpimath:wpimath-java:2025.1.1-beta-1', 'edu.wpi.first.ntcore:ntcore-java:2025.1.1-beta-1', 'edu.wpi.first.cscore:cscore-java:2025.1.1-beta-1', 'edu.wpi.first.cameraserver:cameraserver-java:2025.1.1-beta-1', 'edu.wpi.first.wpiunits:wpiunits-java:2025.1.1-beta-1', 'edu.wpi.first.thirdparty.frc2024.opencv:opencv-java:4.8.0-2']

def library_deps_setup():

    http_file(
        name = "wpilibj_jar_file",
        downloaded_file_path = "wpilibj.jar",  # java_import needs the filename to contain .jar
        sha256 = "eba3b526ce5727725b2a58700b8ff4e792c6681918e43631de72ab612100d48d",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpilibj/wpilibj-java/2025.1.1-beta-1/wpilibj-java-2025.1.1-beta-1.jar"],
    )

    http_file(
        name = "wpilibj_sources_jar_file",
        downloaded_file_path = "wpilibj_sources.jar",
        sha256 = "fb183cd291c136490362b248af54d0aecd2bd47d7beefb218ec0531041f2ad2a",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpilibj/wpilibj-java/2025.1.1-beta-1/wpilibj-java-2025.1.1-beta-1-sources.jar"],
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
        sha256 = "a712c5d89d1920d04a82cc079f74f63f477415be2260e28683d920fdbc38efec",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ni-libraries/visa/2025.0.0/visa-2025.0.0-headers.zip"]
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
        sha256 = "0529b9332268e47bf0b67641e465c953b9aec1e663b2ba96988c4ae9920c91a7",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ni-libraries/visa/2025.0.0/visa-2025.0.0-linuxathena.zip"]
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
        sha256 = "4e5e78c30bcc91abb650701c16b7b4718672e40f2ad8b28299e848ccc8d150f1",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ni-libraries/netcomm/2025.0.0/netcomm-2025.0.0-headers.zip"]
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
        sha256 = "cf64ac6fbeb960865ccfa711efc8d78d31a3bebf1762199156e891368a65c69e",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ni-libraries/netcomm/2025.0.0/netcomm-2025.0.0-linuxathena.zip"]
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
        sha256 = "d21054ef17b22206d777236f60794f0686c61f4a00621e6803570ce7481cdcce",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ni-libraries/chipobject/2025.0.0/chipobject-2025.0.0-headers.zip"]
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
        sha256 = "2ab0f076e85898941c3465015a99be5d1ff823215986f423b2f3eb99bd8ab6ab",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ni-libraries/chipobject/2025.0.0/chipobject-2025.0.0-linuxathena.zip"]
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
        sha256 = "b4e857ef8a6e065f1f06d2350b80a61c8d5e3b51435bd7bd54862259d153c821",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ni-libraries/runtime/2025.0.0/runtime-2025.0.0-linuxathena.zip"]
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
        sha256 = "25ec13e1109839a559668d92fa9fa7067b5706ebdb107600c29c71cfbf77fa5c",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpiutil/wpiutil-cpp/2025.1.1-beta-1/wpiutil-cpp-2025.1.1-beta-1-headers.zip"]
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
        sha256 = "d626e9090bad82dd12207d27f63d5c09a5e35bcd3c4e6d8f2ec990a873ee7af2",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpiutil/wpiutil-cpp/2025.1.1-beta-1/wpiutil-cpp-2025.1.1-beta-1-linuxathena.zip"]
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
        sha256 = "6397ee04d8e25b50ee9d1399799387a579ebc355214539754bb4b5b99dccf63e",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpiutil/wpiutil-cpp/2025.1.1-beta-1/wpiutil-cpp-2025.1.1-beta-1-linuxx86-64.zip"]
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
        sha256 = "bbab37aa11690a5c05cdb10dc0dd4720ee7bbfa20c88ed224126e52bf9acac67",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpiutil/wpiutil-cpp/2025.1.1-beta-1/wpiutil-cpp-2025.1.1-beta-1-windowsx86-64.zip"]
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
        sha256 = "7b8070093e653ab81bb1c22017ef3938af1373735aee920c1eaafdcaa9f57497",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpiutil/wpiutil-cpp/2025.1.1-beta-1/wpiutil-cpp-2025.1.1-beta-1-osxuniversal.zip"]
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
        sha256 = "ffa806d7573a15de30b5d8ccb23bbf7a19efefbe5b283af453ab4f864fd43be6",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpimath/wpimath-cpp/2025.1.1-beta-1/wpimath-cpp-2025.1.1-beta-1-headers.zip"]
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
        sha256 = "56747add2a21a9765a318628ee154f93aaf96337e6d9d00c78b6fa7e21cb60b6",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpimath/wpimath-cpp/2025.1.1-beta-1/wpimath-cpp-2025.1.1-beta-1-linuxathena.zip"]
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
        sha256 = "f776c80c65e42e05881b2f5662fb65b276932139c5eb2814a23b34a28f339b25",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpimath/wpimath-cpp/2025.1.1-beta-1/wpimath-cpp-2025.1.1-beta-1-linuxx86-64.zip"]
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
        sha256 = "7ff5bbf506fa85298b3826075a74addcd125cf4e9973602fb4c8ff01e2d272fe",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpimath/wpimath-cpp/2025.1.1-beta-1/wpimath-cpp-2025.1.1-beta-1-windowsx86-64.zip"]
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
        sha256 = "2fd7aca6d929a9fd8430a58d1f582d901ddcd891542e8cbd5bb63c03e5289ae0",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpimath/wpimath-cpp/2025.1.1-beta-1/wpimath-cpp-2025.1.1-beta-1-osxuniversal.zip"]
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
        sha256 = "b5d833200a7fe4eba0e263b4f4c96245054ad2db8a0e146f7b70c8640b4c98ac",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ntcore/ntcore-cpp/2025.1.1-beta-1/ntcore-cpp-2025.1.1-beta-1-headers.zip"]
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
        sha256 = "37a1372d0040fea7b999f53394f19274e2dc30acc9dd401291df4c58fbfdf698",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ntcore/ntcore-cpp/2025.1.1-beta-1/ntcore-cpp-2025.1.1-beta-1-linuxathena.zip"]
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
        sha256 = "49ab39f65f3d7639b5153bd2efe9a39ecae75ae5314d41e46eadcbfc7627378b",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ntcore/ntcore-cpp/2025.1.1-beta-1/ntcore-cpp-2025.1.1-beta-1-linuxx86-64.zip"]
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
        sha256 = "4d7651d84bfedfb37e4b0665c35e55da0a609c9cc825720be1ca739d6133932e",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ntcore/ntcore-cpp/2025.1.1-beta-1/ntcore-cpp-2025.1.1-beta-1-windowsx86-64.zip"]
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
        sha256 = "8b4d761514e163d93d4dba45038036d31139ca6ddcb15b8db3ad3ea97f4906ca",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/ntcore/ntcore-cpp/2025.1.1-beta-1/ntcore-cpp-2025.1.1-beta-1-osxuniversal.zip"]
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
        sha256 = "be5d1d4d6b2c4c2f4a9ef2fccc258813383a49ca41c8ee75d1f79ba5471b8c08",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/hal/hal-cpp/2025.1.1-beta-1/hal-cpp-2025.1.1-beta-1-headers.zip"]
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
        sha256 = "3c7cc8182c35e047d43f57af029e6b5fbc1b70d1ee93938ec9d1ac7f772401ec",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/hal/hal-cpp/2025.1.1-beta-1/hal-cpp-2025.1.1-beta-1-linuxathena.zip"]
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
        sha256 = "9f4f0ece41aed7f33e3399561f04e83fa2f64cd3f77d5df6bedfb4c16c54d46e",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/hal/hal-cpp/2025.1.1-beta-1/hal-cpp-2025.1.1-beta-1-linuxx86-64.zip"]
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
        sha256 = "8869b1a9737179cbb687360344b448143011fe5596a8f0da64b76341f6ce42f7",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/hal/hal-cpp/2025.1.1-beta-1/hal-cpp-2025.1.1-beta-1-windowsx86-64.zip"]
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
        sha256 = "784622e43a6959e62b91d0765b23d0c898de960a86c0f53a2950082e18a74c20",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/hal/hal-cpp/2025.1.1-beta-1/hal-cpp-2025.1.1-beta-1-osxuniversal.zip"]
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
        sha256 = "a9ea342ad9416fec70bee48d4ad42125f4f9bd7fb335912e84f86f637ec09c06",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/halsim/halsim_gui/2025.1.1-beta-1/halsim_gui-2025.1.1-beta-1-linuxx86-64.zip"]
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
        sha256 = "6e47795c3800222e22a9d8b5d39478fb637f964e7e3f8afe9790023d02b9add6",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/halsim/halsim_gui/2025.1.1-beta-1/halsim_gui-2025.1.1-beta-1-windowsx86-64.zip"]
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
        sha256 = "9a56fe4ce96ee639e76997fae273bfe3189db161150e72cca98f54f62e692a20",
        urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/halsim/halsim_gui/2025.1.1-beta-1/halsim_gui-2025.1.1-beta-1-osxuniversal.zip"]
    )

