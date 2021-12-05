cc_library(
    name = "wpilibio-lib",
    srcs = glob(["src/**/*.cc"]),
    hdrs = glob(["include/**/*.h"]),
    deps = ["//conduit:conduit-schema-cc", "//conduit/api:api-jni", "//third_party/wpilib:hal"],
)

cc_binary(
    name = "wpilibio",
    deps = [":wpilibio-lib"],
    linkshared = True
)