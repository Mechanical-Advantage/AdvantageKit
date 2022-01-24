cc_test(
    name = "size-test",
    size = "small",  # This test should run very fast
    srcs = ["size_test.cc"],
    target_compatible_with = select({
        "//build_tools/platforms:is_athena": ["@platforms//:incompatible"],
        "//conditions:default": [],
    }),
    deps = [
        "//conduit/wpilibio",
        "//third_party/wpilib:hal",
        "@com_google_googletest//:gtest_main",
    ],
)
