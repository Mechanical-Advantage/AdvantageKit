load("@rules_foreign_cc//foreign_cc:defs.bzl", "cmake")

cmake(
    name = "nng",
    generate_args = ["-GNinja"],
    lib_name = "libnng",
    lib_source = "@nng//:all_srcs",
    cache_entries = {
        "NNG_TESTS": "false"
    },
    visibility = ["//visibility:public"]
)
