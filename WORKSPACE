# This loads the rule "http_archive", which is used to download zip files from the web
# and make them available to other rules in our workspace.
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("//build_tools/repo:wpilib_nativezip_utils.bzl", "wpilib_binary_config", "wpilib_nativezip")

# This code loads the "rules_jvm_external" repository into our Bazel workspace.  This is copied in from
# https://github.com/bazelbuild/rules_jvm_external/releases/tag/4.1
RULES_JVM_EXTERNAL_TAG = "4.1"

RULES_JVM_EXTERNAL_SHA = "f36441aa876c4f6427bfb2d1f2d723b48e9d930b62662bf723ddfb8fc80f0140"

http_archive(
    name = "rules_jvm_external",
    sha256 = RULES_JVM_EXTERNAL_SHA,
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

# This installs additional dependencies of "rules_jvm_external" that are needed for it to work properly
load("@rules_jvm_external//:repositories.bzl", "rules_jvm_external_deps")

rules_jvm_external_deps()

load("@rules_jvm_external//:setup.bzl", "rules_jvm_external_setup")

rules_jvm_external_setup()

# This loads the "maven_install" rule from the file that we just told Bazel how to download above.  We then use
# this rule below to tell Bazel what Maven artifacts we want to install.
load("@rules_jvm_external//:defs.bzl", "maven_install")

# All Maven artifacts that we use go here.  The philosophy is that we only ever have one version of any external
# maven library in use across the entire codebase at any time.  If we update it, we make the changes everywhere
# to keep things working.  This eliminates the problem of version conflicts between libraries.

# Bazel will place all libraries downloaded by this into the "@maven" repository.  Example:
# Artifact ID = org.littletonrobotics:somelib:1.0.0  ->  Bazel target = @maven//:org_littletonrobotics_somelib
# From the Bazel docs: "All non-alphanumeric characters are substituted with underscores."
maven_install(
    artifacts = [
        "com.google.guava:guava:30.1.1-jre",  # This is just an example dependency to demonstrate how it works
    ],
    repositories = [
        "https://repo1.maven.org/maven2",  # Maven central
    ],
)

# This tells Bazel how to download the athena (roborio) toolchain for cross compiling
# on 64 bit linux.
http_archive(
    name = "athena_toolchain_linux_x64_files",
    build_file = "@//:build_tools/toolchain/athena_toolchain_linux_x64_files.BUILD",
    sha256 = "00cc58bf0607d71e725919d28e22714ce1920692c4864bc86353fc8139cbf7b7",
    urls = ["https://github.com/wpilibsuite/roborio-toolchain/releases/download/v2021-2/FRC-2021-Linux-Toolchain-7.3.0.tar.gz"],
)

# Same as above for win32 (also supports 64 bit)
http_archive(
    name = "athena_toolchain_windows_x64_files",
    build_file = "@//:build_tools/toolchain/athena_toolchain_windows_x64_files.BUILD",
    sha256 = "f3e9ba32b63d3cd26e242feeb14e878fecbda86c19c12b98c3084c629e06acb3",
    urls = ["https://github.com/wpilibsuite/roborio-toolchain/releases/download/v2021-2/FRC-2021-Windows-Toolchain-7.3.0.zip"],
)

# Same as above for macOS (currently only supports x86_64, native support for arm64 (M1) coming soon? for now use Rosetta 2)
http_archive(
    name = "athena_toolchain_macos_x64_files",
    build_file = "@//:build_tools/toolchain/athena_toolchain_macos_x64_files.BUILD",
    sha256 = "0822ff945ff422b176571cebe7b2dfdc0ef6bf685d3b6f6833db8dc218d992ae",
    urls = ["https://github.com/wpilibsuite/roborio-toolchain/releases/download/v2021-2/FRC-2021-Mac-Toolchain-7.3.0.tar.gz"],
)

# This tells Bazel to add our toolchains to the list of options
register_toolchains(
    "//build_tools/toolchain:athena-cc-toolchain-linux_x64",
    "//build_tools/toolchain:athena-cc-toolchain-windows_x64",
    "//build_tools/toolchain:athena-cc-toolchain-macos_x64",
)

# Below are a bunch of http_archive rules to make various WPILib components available

# HAL components
NI_VERSION = "2020.9.2"

NI_VISA_HEADERS_SHA = "e35e16b13416b2ea389a7abe444d9a60f5315f52661fb84225658780bb938e00"

NI_VISA_ATHENA_SHA = "2cf83ce761145ee57b1b88d817cbadcbf1b34cf1759a813fae1cd135c23e6588"

NI_NETCOMM_HEADERS_SHA = "4baf46aacc5e8587a17f5a27173ec51b017102705932da42a55d6ad1f57f0f93"

NI_NETCOMM_ATHENA_SHA = "6a3dac9d74fe20358812924cc3c2bfaafde189e2b07dc444ba5d38e46ca02b06"

NI_CHIPOBJECT_HEADERS_SHA = "9c196eea0acc1e90545044138818a94b2e3eccfde1ee798e16dfa775a06f09b8"

NI_CHIPOBJECT_ATHENA_SHA = "036060af780bc4d3b852d389a5210ef73df3041b9399762e7be8ea8291bb16e3"

NI_RUNTIME_ATHENA_SHA = "a0aeff05908590b1c63071e02d635010bde54c3abb6dc016c78d5617a72c555a"

WPILIB_VERSION = "2021.3.1"

WPILIB_HAL_HEADERS_SHA = "81b4d98d7ae4f92b2887180aea29ef1e780c5570e3fdbe08e02183e54952bd62"

WPILIB_HAL_ATHENA_SHA = "e9de32abe3739697a3a92963c9eca4bf8755edfb0f11ac95e22d0190a3185f56"

WPILIB_HAL_LINUX_X64_SHA = "48ca6f22deb800170c801944531557c8d109be4501418c719349519405ae6cc2"

WPILIB_WPIUTIL_HEADERS_SHA = "b2a96f7ce07198b139face9dc341c6550d5044fa32f48435b50d986ea5c8ee55"

WPILIB_WPIUTIL_ATHENA_SHA = "ad48bae20f42850938a1758c9f82e54c5cb5e286ad0b09adb701d700bd7f8ec8"

WPILIB_WPIUTIL_LINUX_X64_SHA = "4a20ec638981025c0e41678ac7cea691d5a40121987b1309e6907255636d02cf"

wpilib_nativezip(
    name = "ni_visa",
    binary_configs = [
        wpilib_binary_config(
            libs = ["libvisa.so"],
            platform = "athena",
            sha256 = NI_VISA_ATHENA_SHA,
        ),
    ],
    headers_sha256 = NI_VISA_HEADERS_SHA,
    package = "ni-libraries",
    remote_name = "visa",
    version = NI_VERSION,
    visibility = "@//third_party/ni:__pkg__",
)

wpilib_nativezip(
    name = "ni_netcomm",
    binary_configs = [
        wpilib_binary_config(
            libs = ["libFRC_NetworkCommunication.so.20.0.0"],
            platform = "athena",
            sha256 = NI_NETCOMM_ATHENA_SHA,
        ),
    ],
    headers_sha256 = NI_NETCOMM_HEADERS_SHA,
    package = "ni-libraries",
    remote_name = "netcomm",
    version = NI_VERSION,
    visibility = "@//third_party/ni:__pkg__",
)

wpilib_nativezip(
    name = "ni_chipobject",
    binary_configs = [
        wpilib_binary_config(
            libs = ["libRoboRIO_FRC_ChipObject.so.20.0.0"],
            platform = "athena",
            sha256 = NI_CHIPOBJECT_ATHENA_SHA,
        ),
    ],
    headers_sha256 = NI_CHIPOBJECT_HEADERS_SHA,
    package = "ni-libraries",
    remote_name = "chipobject",
    version = NI_VERSION,
    visibility = "@//third_party/ni:__pkg__",
)

wpilib_nativezip(
    name = "ni_runtime",
    binary_configs = [
        wpilib_binary_config(
            libs = [
                "libni_emb.so.12.0.0",
                "libni_rtlog.so.2.8.0",
                "libNiFpga.so.19.0.0",
                "libNiFpgaLv.so.19.0.0",
                "libnirio_emb_can.so.16.0.0",
                "libniriodevenum.so.19.0.0",
                "libniriosession.so.18.0.0",
                "libNiRioSrv.so.19.0.0",
            ],
            platform = "athena",
            sha256 = NI_RUNTIME_ATHENA_SHA,
        ),
    ],
    package = "ni-libraries",
    remote_name = "runtime",
    version = NI_VERSION,
    visibility = "@//third_party/ni:__pkg__",
)

wpilib_nativezip(
    name = "wpilib_wpiutil",
    binary_configs = [
        wpilib_binary_config(
            libs = ["libwpiutil.so"],
            platform = "athena",
            sha256 = WPILIB_WPIUTIL_ATHENA_SHA,
        ),
        wpilib_binary_config(
            libs = ["libwpiutil.so"],
            platform = "linux_x64",
            sha256 = WPILIB_WPIUTIL_LINUX_X64_SHA
        )
    ],
    headers_sha256 = WPILIB_WPIUTIL_HEADERS_SHA,
    package = "wpiutil",
    remote_name = "wpiutil-cpp",
    version = WPILIB_VERSION,
    visibility = "@//third_party/wpilib:__pkg__",
)

wpilib_nativezip(
    name = "wpilib_hal",
    binary_configs = [
        wpilib_binary_config(
            libs = ["libwpiHal.so"],
            platform = "athena",
            sha256 = WPILIB_HAL_ATHENA_SHA,
        ),
        wpilib_binary_config(
            libs = ["libwpiHal.so"],
            platform = "linux_x64",
            sha256 = WPILIB_HAL_LINUX_X64_SHA,
        )
    ],
    headers_sha256 = WPILIB_HAL_HEADERS_SHA,
    package = "hal",
    remote_name = "hal-cpp",
    version = WPILIB_VERSION,
    visibility = "@//third_party/wpilib:__pkg__"
)