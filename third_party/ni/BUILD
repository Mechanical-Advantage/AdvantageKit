load("//build_tools/cc:cc_superimport.bzl", "cc_superimport")

# VISA.  This is NI's standard IO library for their devices.
cc_superimport(
    name = "visa-athena",
    dynamic_libs = "@ni_visa_athena_files//:binaries_files",
    target_compatible_with = ["//build_tools/platforms:roborio"],
    visibility = ["//visibility:public"],
    deps = ["@ni_visa_headers_files//:headers"],
)

cc_superimport(
    name = "runtime-athena",
    dynamic_libs = "@ni_runtime_athena_files//:binaries_files",
    target_compatible_with = ["//build_tools/platforms:roborio"],
    visibility = ["//visibility:public"],
)

# Chipobject.  This is the "black box" library that provides the low level APIs for interfacing with the RIO's hardware.
cc_superimport(
    name = "chipobject-athena",
    dynamic_libs = "@ni_chipobject_athena_files//:binaries_files",
    target_compatible_with = ["//build_tools/platforms:roborio"],
    visibility = ["//visibility:public"],
    deps = [
        ":runtime-athena",
        "@ni_chipobject_headers_files//:headers",
    ],
)

# Netcomm.  This is the "black box" library that handles communication with the Driver Station.
cc_superimport(
    name = "netcomm-athena",
    dynamic_libs = "@ni_netcomm_athena_files//:binaries_files",
    target_compatible_with = ["//build_tools/platforms:roborio"],
    visibility = ["//visibility:public"],
    deps = [
        ":runtime-athena",
        "@ni_netcomm_headers_files//:headers",
    ],
)
