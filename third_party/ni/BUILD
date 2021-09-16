# VISA.  This is NI's standard IO library for their devices.
cc_import(
    name = "visa-athena-binary",
    shared_library = "@ni_visa_athena//:shared",
)

cc_library(
    name = "visa-athena",
    visibility = ["//visibility:public"],
    deps = [
        ":visa-athena-binary",
        "@ni_visa_headers//:headers",
    ],
)

# Chipobject.  This is the "black box" library that provides the low level APIs for interfacing with the RIO's hardware.
cc_import(
    name = "chipobject-athena-binary",
    shared_library = "@ni_chipobject_athena//:shared",
)

cc_library(
    name = "chipobject-athena",
    visibility = ["//visibility:public"],
    deps = [
        ":chipobject-athena-binary",
        "@ni_chipobject_headers//:headers",
        "@ni_runtime_athena//:ni-runtime-athena",
    ],
)

# Netcomm.  This is the "black box" library that handles communication with the Driver Station.
cc_import(
    name = "netcomm-athena-binary",
    shared_library = "@ni_netcomm_athena//:shared",
)

cc_library(
    name = "netcomm-athena",
    visibility = ["//visibility:public"],
    deps = [
        ":netcomm-athena-binary",
        "@ni_netcomm_headers//:headers",
        "@ni_runtime_athena//:ni-runtime-athena",
    ],
)
