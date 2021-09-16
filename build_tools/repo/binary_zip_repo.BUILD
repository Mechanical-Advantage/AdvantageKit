# Can be applied as the build file of an http_archive to present all ".so" files as a filegroup
# called "shared"

filegroup(
    name = "shared",
    srcs = glob(["**/*.so", "**/*.so.*"]),
    visibility = ["//visibility:public"]
)