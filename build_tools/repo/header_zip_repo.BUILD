# Can be applied as the build file of an http_archive rule to combine all header files into a single cc_library
# with the root directory added to the include path.

cc_library(
    name = "headers",
    hdrs = glob([
        "**/*.h",
        "**/*.hpp",
    ]),
    includes = ["."],
    visibility = ["//visibility:public"],
)