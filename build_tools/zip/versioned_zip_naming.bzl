load("@rules_pkg//:providers.bzl", "PackageVariablesInfo")

def _versioned_zip_naming_impl(ctx):
    version = ctx.var["publishing_version"]
    return PackageVariablesInfo(values = {"publishing_version": version})

versioned_zip_naming = rule(
    implementation = _versioned_zip_naming_impl,
)
