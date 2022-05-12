load("@rules_foreign_cc//foreign_cc:defs.bzl", "cmake")

cmake(
    name = "nng",
    cache_entries = {
        "NNG_TESTS": "OFF",
    },
    generate_args = ["-GNinja"],
    lib_name = select({
        "//build_tools/platforms:is_windows": "nng",
        "//conditions:default": "libnng",
    }),
    lib_source = "@nng//:all_srcs",
    visibility = ["//visibility:public"],
)
