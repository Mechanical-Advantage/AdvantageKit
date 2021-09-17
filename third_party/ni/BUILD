# VISA.  This is NI's standard IO library for their devices.
cc_library(
    name = "visa-athena",
    visibility = ["//visibility:public"],
    deps = [
        "@ni_visa_athena_files//:binaries",
        "@ni_visa_headers_files//:headers",
    ],
)

# Chipobject.  This is the "black box" library that provides the low level APIs for interfacing with the RIO's hardware.
cc_library(
    name = "chipobject-athena",
    visibility = ["//visibility:public"],
    deps = [
        "@ni_chipobject_athena_files//:binaries",
        "@ni_chipobject_headers_files//:headers",
        "@ni_runtime_athena_files//:binaries",
    ],
)

# Netcomm.  This is the "black box" library that handles communication with the Driver Station.
cc_library(
    name = "netcomm-athena",
    visibility = ["//visibility:public"],
    deps = [
        "@ni_netcomm_athena_files//:binaries",
        "@ni_netcomm_headers_files//:headers",
        "@ni_runtime_athena_files//:binaries",
    ],
)
