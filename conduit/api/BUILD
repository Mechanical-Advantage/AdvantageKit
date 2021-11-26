load("//third_party/jni:java_native_headers.bzl", "jni_header_library")

java_library(
    name = "api",
    srcs = glob(["src/**/*.java"]),
    deps = ["//conduit:conduit-schema-java"],
    visibility = ["//visibility:public"],
)

jni_header_library(
    name = "api-jni-athena",
    java_lib = ":api",
    jni_dep = "//third_party/jni:jni-linux_arm32",
    outs = ["org_littletonrobotics_conduit_ConduitJni.h"],
    visibility = ["//visibility:public"]
)