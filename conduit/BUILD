load("//third_party/flatbuffers:build_defs.bzl", "flatbuffer_cc_library", "flatbuffer_java_library")

# All generated targets, to quickly regenerate them
filegroup(
    name = "generated",
    srcs = [
        ":conduit-schema-cc_cc_srcs",
        ":conduit-schema-java_java_srcs",
        "//conduit/api:api-jni_headers",
    ],
)

flatbuffer_java_library(
    name = "conduit-schema-java",
    srcs = ["conduit_schema.fbs"],
    outs = [
        "org/littletonrobotics/conduit/schema/CoreInputs.java",
        "org/littletonrobotics/conduit/schema/DSData.java",
        "org/littletonrobotics/conduit/schema/Joystick.java",
        "org/littletonrobotics/conduit/schema/PDPData.java",
        "org/littletonrobotics/conduit/schema/SystemData.java",
    ],
    visibility = ["//visibility:public"],
)

flatbuffer_cc_library(
    name = "conduit-schema-cc",
    srcs = ["conduit_schema.fbs"],
    outs = ["conduit_schema_generated.h"],
    visibility = ["//visibility:public"],
)
