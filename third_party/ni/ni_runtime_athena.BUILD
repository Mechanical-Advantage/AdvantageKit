# Build script applied to the downloaded zip archive of the NI Runtime library files for Athena.

# This file imports all the .so files inside the zip archive as individual cc_import targets,
# and then combines these targets into one cc_library target which can easily be consumed by higher level libs.

# These libraries don't have headers and should not be used directly, but instead are only meant to be imported
# by the higher level targets defined in //third_party/ni.  Thus, the visibility of the cc_library target enforces this.

cc_import(
    name = "ni_emb",
    shared_library = "linux/athena/shared/libni_emb.so.12.0.0",
)

cc_import(
    name = "ni_rtlog",
    shared_library = "linux/athena/shared/libni_rtlog.so.2.8.0",
)

cc_import(
    name = "NiFpga",
    shared_library = "linux/athena/shared/libNiFpga.so.19.0.0",
)

cc_import(
    name = "NiFpgaLv",
    shared_library = "linux/athena/shared/libNiFpgaLv.so.19.0.0",
)

cc_import(
    name = "nirio_emb_can",
    shared_library = "linux/athena/shared/libnirio_emb_can.so.16.0.0",
)

cc_import(
    name = "niriodevenum",
    shared_library = "linux/athena/shared/libniriodevenum.so.19.0.0",
)

cc_import(
    name = "niriosession",
    shared_library = "linux/athena/shared/libniriosession.so.18.0.0",
)

cc_import(
    name = "NiRioSrv",
    shared_library = "linux/athena/shared/libNiRioSrv.so.19.0.0",
)

cc_library(
    name = "ni-runtime-athena",
    visibility = ["@//third_party/ni:__pkg__"],  # Only allow //third_party/ni to access
    deps = [
        # Pull all the imported .so files together
        ":NiFpga",
        ":NiFpgaLv",
        ":NiRioSrv",
        ":ni_emb",
        ":ni_rtlog",
        ":nirio_emb_can",
        ":niriodevenum",
        ":niriosession",
    ],
)
