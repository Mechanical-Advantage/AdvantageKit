load("//third_party/jni:java_native_headers.bzl", "jni_header_library")
load("@rules_jvm_external//:defs.bzl", "java_export")

java_export(
    name = "api",
    srcs = glob(["src/**/*.java"]),
    maven_coordinates = "org.littletonrobotics.akit.conduit:conduit-api:0.0.1",
    visibility = ["//visibility:public"],
    deps = ["//conduit:conduit-schema-java"],
)

jni_header_library(
    name = "api-jni",
    outs = ["org_littletonrobotics_conduit_ConduitJni.h"],
    java_lib = ":api",
    jni_dep = "//third_party/jni",
    visibility = ["//visibility:public"],
)
