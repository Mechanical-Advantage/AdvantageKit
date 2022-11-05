# This loads the rule "http_archive", which is used to download zip files from the web
# and make them available to other rules in our workspace.
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

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
    sha256 = "512da68b344a8e66e598ff647239a6ab39b6330c0c4152183410bd65326c7923",
    strip_prefix = "bazel-compile-commands-extractor-e704e82375048df67200b96f83e3e5e7bda8897e",
    url = "https://github.com/hedronvision/bazel-compile-commands-extractor/archive/e704e82375048df67200b96f83e3e5e7bda8897e.tar.gz",
)

load("@hedron_compile_commands//:workspace_setup.bzl", "hedron_compile_commands_setup")

hedron_compile_commands_setup()

# library_deps.bzl contains definitions for MAVEN_ARTIFACTS (maven central), FRCMAVEN_ARTIFACTS (wpilib maven server),
# the wpilibj jar and sources file, and http_archive rules to download native wpilib dependencies.
# Due to Bazel's requirement that all artifacts need an sha256 to verify they haven't changed, we have a Python script which
# generates this file.  Please do not modify this file directly, instead modify the generator script and regenerate it.
# An internet connection is required to run the generator as it requests the sha256 checksums for several files
# and re-downloads all maven dependencies.

# To add dependencies to any of the above categories, please see generate_library_deps.bzl, and then execute it when done.
load(":library_deps.bzl", "FRCMAVEN_ARTIFACTS", "MAVEN_ARTIFACTS", "library_deps_setup")

library_deps_setup()

# All Maven artifacts that we use go here.  The philosophy is that we only ever have one version of any external
# maven library in use across the entire codebase at any time.  If we update it, we make the changes everywhere
# to keep things working.  This eliminates the problem of version conflicts between libraries.

# Bazel will place all libraries downloaded by this into the "@maven" repository.  Example:
# Artifact ID = org.littletonrobotics:somelib:1.0.0  ->  Bazel target = @maven//:org_littletonrobotics_somelib
# From the Bazel docs: "All non-alphanumeric characters are substituted with underscores."

# Please see above about library_deps to find where the list of maven artifacts is located.

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

BAZEL_TOOLCHAIN_TAG = "0.7.2"

BAZEL_TOOLCHAIN_SHA = "f7aa8e59c9d3cafde6edb372d9bd25fb4ee7293ab20b916d867cd0baaa642529"

http_archive(
    name = "com_grail_bazel_toolchain",
    canonical_id = BAZEL_TOOLCHAIN_TAG,
    sha256 = BAZEL_TOOLCHAIN_SHA,
    strip_prefix = "bazel-toolchain-{tag}".format(tag = BAZEL_TOOLCHAIN_TAG),
    url = "https://github.com/grailbio/bazel-toolchain/archive/{tag}.tar.gz".format(tag = BAZEL_TOOLCHAIN_TAG),
)

load("@com_grail_bazel_toolchain//toolchain:deps.bzl", "bazel_toolchain_dependencies")

bazel_toolchain_dependencies()

load("@com_grail_bazel_toolchain//toolchain:rules.bzl", "llvm_toolchain")

llvm_toolchain(
    name = "llvm_toolchain",
    llvm_version = "13.0.0",
    stdlib = {
        "linux-x86_64": "stdc++",
        "linux-aarch64": "stdc++",
    },
)

load("@llvm_toolchain//:toolchains.bzl", "llvm_register_toolchains")

llvm_register_toolchains()

http_archive(
    name = "rules_foreign_cc",
    sha256 = "6041f1374ff32ba711564374ad8e007aef77f71561a7ce784123b9b4b88614fc",
    strip_prefix = "rules_foreign_cc-0.8.0",
    url = "https://github.com/bazelbuild/rules_foreign_cc/archive/0.8.0.tar.gz",
)

load("@rules_foreign_cc//foreign_cc:repositories.bzl", "rules_foreign_cc_dependencies")

# This sets up some common toolchains for building targets. For more details, please see
# https://bazelbuild.github.io/rules_foreign_cc/0.8.0/flatten.html#rules_foreign_cc_dependencies
rules_foreign_cc_dependencies()

_ALL_CONTENT = """
filegroup(
    name = "all_srcs",
    srcs = glob(["**"]),
    visibility = ["//visibility:public"],
)
"""

# pcre source code repository
http_archive(
    name = "nng",
    build_file_content = _ALL_CONTENT,
    sha256 = "652ff3a2dbaeae194942205c369e9259e2b5cb5985d679d744cbfb95d1c807a3",
    strip_prefix = "nng-1.5.2",
    urls = [
        "https://github.com/nanomsg/nng/archive/refs/tags/v1.5.2.zip",
    ],
)

# This tells Bazel how to download the athena (roborio) toolchain for cross compiling
# on 64 bit linux.
http_archive(
    name = "athena_toolchain_linux_x64_files",
    build_file = "@//:build_tools/toolchain/athena_toolchain_linux_x64_files.BUILD",
    sha256 = "56bd5b53a4149b06fd4cf675dc0596668af47ca8da72c461b7d772ada35cbdc5",
    urls = ["https://github.com/wpilibsuite/opensdk/releases/download/v2023-5/cortexa9_vfpv3-roborio-academic-2023-x86_64-linux-gnu-Toolchain-12.1.0.tgz"],
)

# Same as above for win32 (also supports 64 bit)
http_archive(
    name = "athena_toolchain_windows_x64_files",
    build_file = "@//:build_tools/toolchain/athena_toolchain_windows_x64_files.BUILD",
    sha256 = "04049728801e97fa41a8aa0837e40103c89391d0479f106aad6f959bbccd9da5",
    urls = ["https://github.com/wpilibsuite/opensdk/releases/download/v2023-5/cortexa9_vfpv3-roborio-academic-2023-x86_64-w64-mingw32-Toolchain-12.1.0.zip"],
)

# Same as above for macOS (currently only supports x86_64, native support for arm64 (M1) coming soon? for now use Rosetta 2)
http_archive(
    name = "athena_toolchain_macos_x64_files",
    build_file = "@//:build_tools/toolchain/athena_toolchain_macos_x64_files.BUILD",
    sha256 = "511a64461bfdec00bacb9abe1470f1c112fc9773b29dbb275b1dc9560b973146",
    urls = ["https://github.com/wpilibsuite/opensdk/releases/download/v2023-5/cortexa9_vfpv3-roborio-academic-2023-x86_64-apple-darwin-Toolchain-12.1.0.tgz"],
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
