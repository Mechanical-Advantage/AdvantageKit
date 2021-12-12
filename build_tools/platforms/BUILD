package(default_visibility = ["//visibility:public"])

# Creates a new "constraint" category, which can have one or more "values"
# A constraint is used to define the properties of a specific platform.
# All platforms have an operating system and a CPU type, but we often wish to
# define additional properties of a platform to more completely specify it.
# In this case, we create a category called "device" to differentiate between
# different physical device types.  There can be many devices running Linux
# and using an ARMv7 CPU, but the roboRIO for example is a very specific device
# within that set.  Since the roboRIO has a specific compiler, we need a way
# to differentiate it from other Linux ARM devices.
constraint_setting(name = "device")

# See the above comment to understand constraints.  This creates the roboRIO
# value within the device category.
constraint_value(
    name = "roborio",
    constraint_setting = ":device",
)

# This defines the platform, which ties together multiple constraint values
# to define a complete platform.  In this case, "Athena" (the codename for the roboRIO)
# is a platform that runs Linux, has an ARMv7 CPU, and has a device type of roboRIO.
platform(
    name = "athena",
    constraint_values = [
        "@platforms//os:linux",
        "@platforms//cpu:arm",
        ":roborio",
    ],
)

config_setting(
    name = "is_athena",
    constraint_values = [":roborio"],
    visibility = ["//visibility:public"],
)

config_setting(
    name = "is_linux",
    constraint_values = ["@platforms//os:linux", "@platforms//cpu:x86_64"],
    visibility = ["//visibility:public"],
)

config_setting(
    name = "is_linux_any",
    constraint_values = ["@platforms//os:linux"],
    visibility = ["//visibility:public"],
)

config_setting(
    name = "is_windows",
    constraint_values = ["@platforms//os:windows"],
    visibility = ["//visibility:public"],
)

config_setting(
    name = "is_macos",
    constraint_values = ["@platforms//os:macos"],
    visibility = ["//visibility:public"],
)
