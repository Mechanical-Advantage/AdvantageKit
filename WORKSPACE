# This loads the rule "http_archive", which is used to download zip files from the web
# and make them available to other rules in our workspace.
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive", "http_file")
load("//build_tools/repo:wpilib_nativezip_utils.bzl", "wpilib_binary_config", "wpilib_nativezip")

http_archive(
    name = "bazel_skylib",
    sha256 = "c6966ec828da198c5d9adbaa94c05e3a1c7f21bd012a0b29ba8ddbccb2c93b0d",
    urls = [
        "https://github.com/bazelbuild/bazel-skylib/releases/download/1.1.1/bazel-skylib-1.1.1.tar.gz",
        "https://mirror.bazel.build/github.com/bazelbuild/bazel-skylib/releases/download/1.1.1/bazel-skylib-1.1.1.tar.gz",
    ],
)

load("@bazel_skylib//:workspace.bzl", "bazel_skylib_workspace")

bazel_skylib_workspace()

# This code loads the "rules_jvm_external" repository into our Bazel workspace.  This is copied in from
# https://github.com/bazelbuild/rules_jvm_external/releases/tag/4.1

RULES_JVM_EXTERNAL_TAG = "4.2"

RULES_JVM_EXTERNAL_SHA = "cd1a77b7b02e8e008439ca76fd34f5b07aecb8c752961f9640dea15e9e5ba1ca"

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

# rules_pkg, used to create zip files for our nativezip deployment
http_archive(
    name = "rules_pkg",
    sha256 = "a89e203d3cf264e564fcb96b6e06dd70bc0557356eb48400ce4b5d97c2c3720d",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/rules_pkg/releases/download/0.5.1/rules_pkg-0.5.1.tar.gz",
        "https://github.com/bazelbuild/rules_pkg/releases/download/0.5.1/rules_pkg-0.5.1.tar.gz",
    ],
)

load("@rules_pkg//:deps.bzl", "rules_pkg_dependencies")

rules_pkg_dependencies()

# Hedron's Compile Commands Extractor for Bazel
# https://github.com/hedronvision/bazel-compile-commands-extractor
http_archive(
    name = "hedron_compile_commands",

    # Replace the commit hash in both places (below) with the latest. 
    # Even better, set up Renovate and let it do the work for you (see "Suggestion: Updates" below).
    url = "https://github.com/hedronvision/bazel-compile-commands-extractor/archive/e704e82375048df67200b96f83e3e5e7bda8897e.tar.gz",
    strip_prefix = "bazel-compile-commands-extractor-e704e82375048df67200b96f83e3e5e7bda8897e",
)
load("@hedron_compile_commands//:workspace_setup.bzl", "hedron_compile_commands_setup")
hedron_compile_commands_setup()

# All Maven artifacts that we use go here.  The philosophy is that we only ever have one version of any external
# maven library in use across the entire codebase at any time.  If we update it, we make the changes everywhere
# to keep things working.  This eliminates the problem of version conflicts between libraries.

# Bazel will place all libraries downloaded by this into the "@maven" repository.  Example:
# Artifact ID = org.littletonrobotics:somelib:1.0.0  ->  Bazel target = @maven//:org_littletonrobotics_somelib
# From the Bazel docs: "All non-alphanumeric characters are substituted with underscores."

# Maven artifact lists are stored in library_deps.bzl.  To add artifacts, go there.
# After adding artifacts, make sure to "repin" the maven dependencies by running "bazel run @unpinned_maven//:pin"
# (or "bazel run @unpinned_frcmaven//:pin" for the frcmaven repo).
# Newly added maven dependencies will NOT be downloaded until the appropriate command is run!
load(":library_deps.bzl", "FRCMAVEN_ARTIFACTS", "MAVEN_ARTIFACTS", "WPILIB_VERSION")

maven_install(
    artifacts = MAVEN_ARTIFACTS,
    fetch_sources = True,
    maven_install_json = "//:maven_install.json",
    repositories = [
        "https://repo1.maven.org/maven2",  # Maven central
    ],
)

maven_install(
    name = "frcmaven",
    artifacts = FRCMAVEN_ARTIFACTS,
    fetch_sources = True,
    maven_install_json = "//:frcmaven_install.json",
    repositories = [
        "https://frcmaven.wpi.edu/artifactory/release",
    ],
)

load("@maven//:defs.bzl", "pinned_maven_install")

pinned_maven_install()

load("@frcmaven//:defs.bzl", frcmaven_pinned_maven_install = "pinned_maven_install")

frcmaven_pinned_maven_install()

# Googletest - The C++ testing framework we use.  This http_archive rule pulls that in:

# Googletest depends on rules_python
http_archive(
    name = "rules_python",
    sha256 = "cd6730ed53a002c56ce4e2f396ba3b3be262fd7cb68339f0377a45e8227fe332",
    url = "https://github.com/bazelbuild/rules_python/releases/download/0.5.0/rules_python-0.5.0.tar.gz",
)

# Googletest depends on abseil - Google's C++ standard library
http_archive(
    name = "com_google_absl",
    sha256 = "b4e20d9e752a75c10636675691b1e9c2698e0764cb404987d0ffa77223041c19",
    strip_prefix = "abseil-cpp-215105818dfde3174fe799600bb0f3cae233d0bf",
    urls = ["https://github.com/abseil/abseil-cpp/archive/215105818dfde3174fe799600bb0f3cae233d0bf.zip"],
)

http_archive(
    name = "com_google_googletest",
    sha256 = "8daa1a71395892f7c1ec5f7cb5b099a02e606be720d62f1a6a98f8f8898ec826",
    strip_prefix = "googletest-e2239ee6043f73722e7aa812a459f54a28552929",
    urls = ["https://github.com/google/googletest/archive/e2239ee6043f73722e7aa812a459f54a28552929.zip"],
)

# This makes WPILib's source repo (allwpilib) available as a repository within our Bazel workspace.
#new_git_repository(
#    name = "allwpilib",
#    commit = "936d3b9f838dfbe0db5332e5bd2038eeac2dbe0b", # v2020.3.1
#    shallow_since = "1619320959 -0700",
#    build_file = "@//:third_party/wpilib/BUILD.allwpilib",
#    remote = "https://github.com/wpilibsuite/allwpilib"
#)

# This tells Bazel how to download the athena (roborio) toolchain for cross compiling
# on 64 bit linux.
http_archive(
    name = "athena_toolchain_linux_x64_files",
    build_file = "@//:build_tools/toolchain/athena_toolchain_linux_x64_files.BUILD",
    sha256 = "b27cde302e46d11524aedf664129bc3ac7df02a78d0f9e4ab3f1feb40d667ab4",
    urls = ["https://github.com/wpilibsuite/roborio-toolchain/releases/download/v2022-1/FRC-2022-Linux-Toolchain-7.3.0.tar.gz"],
)

# Same as above for win32 (also supports 64 bit)
http_archive(
    name = "athena_toolchain_windows_x64_files",
    build_file = "@//:build_tools/toolchain/athena_toolchain_windows_x64_files.BUILD",
    sha256 = "3a8815d9c715e7f0f5d2106e4f16282863a3ff63121d259703b281881daea683",
    urls = ["https://github.com/wpilibsuite/roborio-toolchain/releases/download/v2022-1/FRC-2022-Windows64-Toolchain-7.3.0.zip"],
)

# Same as above for macOS (currently only supports x86_64, native support for arm64 (M1) coming soon? for now use Rosetta 2)
http_archive(
    name = "athena_toolchain_macos_x64_files",
    build_file = "@//:build_tools/toolchain/athena_toolchain_macos_x64_files.BUILD",
    sha256 = "47d29989d2618c0fc439b72e8d3d734b93952da4136dd05a7648af19662700b7",
    urls = ["https://github.com/wpilibsuite/roborio-toolchain/releases/download/v2022-1/FRC-2022-Mac-Toolchain-7.3.0.tar.gz"],
)

# Toolchains for compiling flatbuffers (.fbs) to generated source files
http_archive(
    name = "flatc_toolchain_linux_x64_files",
    build_file = "@//:build_tools/toolchain/flatc/flatc_toolchain_linux_x64_files.BUILD",
    sha256 = "d7a4a866b7380e175f820c5f8d93d1f777c4ab48beccfa8366a45c597af60af7",
    urls = ["https://github.com/google/flatbuffers/releases/download/v2.0.0/Linux.flatc.binary.clang++-9.zip"],
)

http_archive(
    name = "flatc_toolchain_windows_x64_files",
    build_file = "@//:build_tools/toolchain/flatc/flatc_toolchain_windows_x64_files.BUILD",
    sha256 = "dff388932d14c2d4b66a94c3fc6b4f5663041d098620e942cc1e59ddb1eda572",
    urls = ["https://github.com/google/flatbuffers/releases/download/v2.0.0/Windows.flatc.binary.zip"],
)

http_archive(
    name = "flatc_toolchain_macos_x64_files",
    build_file = "@//:build_tools/toolchain/flatc/flatc_toolchain_macos_x64_files.BUILD",
    sha256 = "1b40cb2e08b6fbcfd7398fecdf4dcb52835d197d93f8116f9a18b270e0a55ae8",
    urls = ["https://github.com/google/flatbuffers/releases/download/v2.0.0/Mac.flatc.binary.zip"],
)

# This tells Bazel to add our toolchains to the list of options
register_toolchains(
    "//build_tools/toolchain:athena-cc-toolchain-linux_x64",
    "//build_tools/toolchain:athena-cc-toolchain-windows_x64",
    "//build_tools/toolchain:athena-cc-toolchain-macos_x64",
    "//build_tools/toolchain/flatc:flatc_toolchain_linux_x64",
    "//build_tools/toolchain/flatc:flatc_toolchain_windows_x64",
    "//build_tools/toolchain/flatc:flatc_toolchain_macos_x64",
)

# Below are a bunch of http_archive rules to make various WPILib components available

# HAL components
NI_VERSION = "2022.2.3"

NI_VISA_HEADERS_SHA = "7e428729d299cae5b0e1acd34f38609af6544c83095c3b4b5897d0183eb1a15f"

NI_VISA_ATHENA_SHA = "014fff5a4684f3c443cbed8c52f19687733dd7053a98a1dad89941801e0b7930"

NI_NETCOMM_HEADERS_SHA = "fddb8924887bd46c1d51099327085e821f30c1b2feefd168f1211675c17937d8"

NI_NETCOMM_ATHENA_SHA = "f56c2fc0943f27f174642664b37fb4529d6f2b6405fe41a639a4c9f31e175c73"

NI_CHIPOBJECT_HEADERS_SHA = "c43e7e9eb7a48c1f97c6b21a0474ec7c8527fbeb3d6c5f62d4d9566771afe25e"

NI_CHIPOBJECT_ATHENA_SHA = "fdf47ae5ce052edd82ba2b6e007faabf9286f87b079e3789afc3235733d5475c"

NI_RUNTIME_ATHENA_SHA = "186d1b41e96c5d761705221afe0b9f488d1ce86e8149a7b0afdc3abc93097266"

WPILIBJ_JAR_SHA = "4f1d73613dca343a963101368e255b2746f7363f3c0d403a661cef59c0a73f5f"

WPILIBJ_SOURCES_JAR_SHA = "696603b7b97ff6502c361a3c34195e4426e8ea09ae6f160e8301087ff05d4dc4"

WPILIB_HAL_HEADERS_SHA = "3b68b20848dbe2db9c6b733e68fa0a46346126ab7f75e64326a6f728560c9233"

WPILIB_HAL_ATHENA_SHA = "509713a4fe7be149d062c5e2fac5b45e9d77c4830977e6a15cd26b47e698cfe0"

WPILIB_HAL_LINUX_X64_SHA = "9881e07c791bfe650b1d86153925095970028e5c9d327b66a10be16dd6165f75"

WPILIB_HAL_WINDOWS_X64_SHA = "6019034d8330b6459b9da3d0ef19462b17ef5dea189aaaed99f522a51c486e3f"

WPILIB_HAL_MACOS_X64_SHA = "4162062e2b2d547d289dc7400d58d682d2b101cfbb031b4ab3ddd7f5993df625"

WPILIB_WPIUTIL_HEADERS_SHA = "143f415335d3477f9de728bc753da7f76bf951519bf54bb81aada7dafd66dc54"

WPILIB_WPIUTIL_ATHENA_SHA = "ede8763ecd1aea8d35d4e27db0500364e6b7e0aeb410fed4efd9848a1b7b16fe"

WPILIB_WPIUTIL_LINUX_X64_SHA = "8cad1f53b958688afe516c892ea2a53b60934b2babc0772761adc0df42d6b4e0"

WPILIB_WPIUTIL_WINDOWS_X64_SHA = "0d9de2d7073dfbb045ef89e98c05917e64ac4e1e075b6239caa702a70d39498c"

WPILIB_WPIUTIL_MACOS_X64_SHA = "c87bdf68e3531ed44ef326d9ca498bf9303fcb313d6787ec80f474bac4df7e1d"

WPILIB_WPIMATH_LINUX_X64_SHA = "006cdb50c4d10e78439a6ca00ab7636d238855cb6a222655094cdbf0d5224844"

WPILIB_WPIMATH_WINDOWS_X64_SHA = "4a8be8d9ad26fb09b536c61fe973ff6189b340dd67a2472b60c42b6b91bc5dc5"

WPILIB_WPIMATH_MACOS_X64_SHA = "9a76767fdfaabaf406c8c0421e702ebff1f7cf1ac5940f1039bd0ec882c02fb6"

WPILIB_NTCORE_LINUX_X64_SHA = "4292ae7a805a553d601bbbcf0f4cac374b7a6b21bc84d3b8bebba7d048fe96b0"

WPILIB_NTCORE_WINDOWS_X64_SHA = "51c3004006af77279bdce2d6c2eb106a465e64a2bc126ff8ffd964dc22f2e866"

WPILIB_NTCORE_MACOS_X64_SHA = "e6418a3faa50a0e14a420a7d330ac2a935b2fcdef6dca040c967bdcd883edf2c"

WPILIB_HALSIM_LINUX_X64_SHA = "c27ad874e911e8fb17b4a63ab8cce61ce56c25e8a2ff9a8ab5b266a34d075a3c"

WPILIB_HALSIM_WINDOWS_X64_SHA = "7fc55879c1e9fbdbcb4621e35843f1f0577a22820d30e0bd71787405eb442a0f"

WPILIB_HALSIM_MACOS_X64_SHA = "f848dc434c10031b33155c554f3ec8336636ea271ec5288bdf404ee748fa4814"

http_file(
    name = "wpilibj_jar_file",
    downloaded_file_path = "wpilibj.jar",  # java_import needs the filename to contain .jar
    sha256 = WPILIBJ_JAR_SHA,
    urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpilibj/wpilibj-java/%s/wpilibj-java-%s.jar" % (WPILIB_VERSION, WPILIB_VERSION)],
)

http_file(
    name = "wpilibj_sources_jar_file",
    downloaded_file_path = "wpilibj_sources.jar",
    sha256 = WPILIBJ_SOURCES_JAR_SHA,
    urls = ["https://frcmaven.wpi.edu/artifactory/release/edu/wpi/first/wpilibj/wpilibj-java/%s/wpilibj-java-%s-sources.jar" % (WPILIB_VERSION, WPILIB_VERSION)],
)

wpilib_nativezip(
    name = "ni_visa",
    binary_configs = [
        wpilib_binary_config(
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
            platform = "athena",
            sha256 = WPILIB_WPIUTIL_ATHENA_SHA,
        ),
        wpilib_binary_config(
            platform = "linux_x64",
            sha256 = WPILIB_WPIUTIL_LINUX_X64_SHA,
        ),
        wpilib_binary_config(
            platform = "windows_x64",
            sha256 = WPILIB_WPIUTIL_WINDOWS_X64_SHA,
        ),
        wpilib_binary_config(
            platform = "macos_x64",
            sha256 = WPILIB_WPIUTIL_MACOS_X64_SHA,
        ),
    ],
    headers_sha256 = WPILIB_WPIUTIL_HEADERS_SHA,
    package = "wpiutil",
    remote_name = "wpiutil-cpp",
    version = WPILIB_VERSION,
    visibility = "@//third_party/wpilib:__pkg__",
)

wpilib_nativezip(
    name = "wpilib_wpimath",
    binary_configs = [
        wpilib_binary_config(
            platform = "linux_x64",
            sha256 = WPILIB_WPIMATH_LINUX_X64_SHA,
        ),
        wpilib_binary_config(
            platform = "windows_x64",
            sha256 = WPILIB_WPIMATH_WINDOWS_X64_SHA,
        ),
        wpilib_binary_config(
            platform = "macos_x64",
            sha256 = WPILIB_WPIMATH_MACOS_X64_SHA,
        ),
    ],
    package = "wpimath",
    remote_name = "wpimath-cpp",
    version = WPILIB_VERSION,
    visibility = "@//third_party/wpilib:__pkg__",
)

wpilib_nativezip(
    name = "wpilib_ntcore",
    binary_configs = [
        wpilib_binary_config(
            platform = "linux_x64",
            sha256 = WPILIB_NTCORE_LINUX_X64_SHA,
        ),
        wpilib_binary_config(
            platform = "windows_x64",
            sha256 = WPILIB_NTCORE_WINDOWS_X64_SHA,
        ),
        wpilib_binary_config(
            platform = "macos_x64",
            sha256 = WPILIB_NTCORE_MACOS_X64_SHA,
        ),
    ],
    package = "ntcore",
    remote_name = "ntcore-cpp",
    version = WPILIB_VERSION,
    visibility = "@//third_party/wpilib:__pkg__",
)

wpilib_nativezip(
    name = "wpilib_hal",
    binary_configs = [
        wpilib_binary_config(
            platform = "athena",
            sha256 = WPILIB_HAL_ATHENA_SHA,
        ),
        wpilib_binary_config(
            platform = "linux_x64",
            sha256 = WPILIB_HAL_LINUX_X64_SHA,
        ),
        wpilib_binary_config(
            platform = "windows_x64",
            sha256 = WPILIB_HAL_WINDOWS_X64_SHA,
        ),
        wpilib_binary_config(
            platform = "macos_x64",
            sha256 = WPILIB_HAL_MACOS_X64_SHA,
        ),
    ],
    headers_sha256 = WPILIB_HAL_HEADERS_SHA,
    package = "hal",
    remote_name = "hal-cpp",
    version = WPILIB_VERSION,
    visibility = "@//third_party/wpilib:__pkg__",
)

wpilib_nativezip(
    name = "wpilib_halsim_gui",
    binary_configs = [
        wpilib_binary_config(
            platform = "linux_x64",
            sha256 = WPILIB_HALSIM_LINUX_X64_SHA,
        ),
        wpilib_binary_config(
            platform = "windows_x64",
            sha256 = WPILIB_HALSIM_WINDOWS_X64_SHA,
        ),
        wpilib_binary_config(
            platform = "macos_x64",
            sha256 = WPILIB_HALSIM_MACOS_X64_SHA,
        ),
    ],
    package = "halsim",
    remote_name = "halsim_gui",
    version = WPILIB_VERSION,
    visibility = "@//third_party/wpilib:__pkg__",
)
