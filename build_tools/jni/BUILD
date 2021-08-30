package(default_visibility = ["//visibility:public"])

# This file defines the JNI header library for various platforms.  For simplicty,
# we have simply copied in the various header files for the platforms we support into
# this repository.  JNI uses a main header, "jni.h", for all platforms, and a "machine dependent" header,
# called "jni_md.h", which has different contents for different platforms.

# For reference, the headers here are pulled from AdoptOpenJDK builds, JDK version 11
# for various
cc_library(
    name = "jni-linux_arm32",
    hdrs = [
        "include/jni.h",
        "platform/linux_arm32/jni_md.h",
    ],
    includes = [
        "include",
        "platform/linux_arm32",
    ],
)

cc_library(
    name = "jni-linux_x64",
    hdrs = [
        "include/jni.h",
        "platform/linux_x64/jni_md.h",
    ],
    includes = [
        "include",
        "platform/linux_x64",
    ],
)

cc_library(
    name = "jni-macos_x64",
    hdrs = [
        "include/jni.h",
        "platform/macos_x64/jni_md.h",
    ],
    includes = [
        "include",
        "platform/macos_x64",
    ],
)

cc_library(
    name = "jni-windows_x64",
    hdrs = [
        "include/jni.h",
        "platform/windows_x64/jni_md.h",
    ],
    includes = [
        "include",
        "platform/windows_x64",
    ],
)
