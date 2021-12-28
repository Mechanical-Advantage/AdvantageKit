def java_sigcheck_test(name, original_library, patch_library, size = "small"):
    native.java_test(
        name = name,
        args = [
            "$(location %s)" % patch_library,
            "$(location %s)" % original_library,
        ],
        data = [
            patch_library,
            original_library,
        ],
        main_class = "Sigcheck",
        use_testrunner = False,
        runtime_deps = [
            original_library,
            patch_library,
            "//build_tools/jpatch:sigcheck",
        ],
    )
