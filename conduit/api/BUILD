load("//third_party/jni:java_native_headers.bzl", "jni_header_library")
load("//build_tools/repo:java_export.bzl", "java_export")

java_library(
    name = "api",
    srcs = glob(["src/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = ["//conduit:conduit-schema-java"],
)

java_export(
    name = "api-export",
    maven_coordinates = "org.littletonrobotics.akit.conduit:conduit-api:dev",
    visibility = ["//visibility:public"],
    runtime_deps = [":api"],
)

jni_header_library(
    name = "api-jni",
    outs = ["org_littletonrobotics_conduit_ConduitJni.h"],
    java_lib = ":api",
    jni_dep = "//third_party/jni",
    visibility = ["//visibility:public"],
)
