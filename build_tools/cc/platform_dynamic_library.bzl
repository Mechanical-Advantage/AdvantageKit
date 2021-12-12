def cc_platform_dynamic_library(name, libname, deps, visibility):
    linux_libname = "lib%s.so" % libname
    win_libname = "%s.dll" % libname
    macos_libname = "lib%s.dylib" % libname

    native.cc_binary(
        name = linux_libname,
        deps = deps,
        linkshared = True,
    )

    native.cc_binary(
        name = win_libname,
        deps = deps,
        linkshared = True,
    )

    native.cc_binary(
        name = macos_libname,
        deps = deps,
        linkshared = True,
    )

    native.alias(
        name = name,
        actual = select({
            "//build_tools/platforms:is_linux_any": ":%s" % linux_libname,
            "//build_tools/platforms:is_windows": ":%s" % win_libname,
            "//build_tools/platforms:is_macos": ":%s" % macos_libname,
        }),
        visibility = visibility,
    )
