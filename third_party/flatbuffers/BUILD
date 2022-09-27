licenses(["notice"])

package(
    default_visibility = ["//visibility:public"],
)

exports_files([
    "LICENSE",
])

# Library used by flatbuffer_cc_library rules.
cc_library(
    name = "runtime_cc",
    hdrs = [
        "include/flatbuffers/base.h",
        "include/flatbuffers/flatbuffers.h",
        "include/flatbuffers/flexbuffers.h",
        "include/flatbuffers/stl_emulation.h",
        "include/flatbuffers/util.h",
    ],
    includes = ["include"],
    linkstatic = 1,
    strip_include_prefix = "/third_party/flatbuffers/include",
)

java_library(
    name = "runtime_java",
    srcs = glob(["java/**/*.java"]),
)
