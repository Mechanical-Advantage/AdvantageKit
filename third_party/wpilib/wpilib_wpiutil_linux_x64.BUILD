# Build script applied to the downloaded zip archive of the WPILib wpiutil library files for Athena.

# This file imports all the .so files inside the zip archive as individual cc_import targets,
# and then combines these targets into one cc_library target which can easily be consumed by higher level libs.

cc_import(
    name = "wpiutil",
    shared_library = "linux/x86-64/shared/libwpiutil.so",

)

cc_library(
    name = "wpilib-wpiutil-linux_x64",
    deps = [":wpiutil"],
    visibility = ["@//third_party/wpilib:__pkg__"]
)