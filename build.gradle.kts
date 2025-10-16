tasks.register<Copy>("generateVsCodeConfig") {
    dependsOn(gradle.includedBuild("akit").task(":generateVsCodeConfig"))

    from(gradle.includedBuild("akit").projectDir.resolve("build/vscodeconfig.json"))
    into(layout.buildDirectory)
}

tasks.register("build") {
    dependsOn(gradle.includedBuild("akit").task(":build"))
    dependsOn(gradle.includedBuild("akit").task(":generateVsCodeConfig"))
}

tasks.register("spotlessCheck") {
    dependsOn(gradle.includedBuild("akit").task(":spotlessCheck"))
    dependsOn(gradle.includedBuild("template_projects").task(":spotlessCheck"))
}

tasks.register("spotlessApply") {
    dependsOn(gradle.includedBuild("akit").task(":spotlessApply"))
    dependsOn(gradle.includedBuild("template_projects").task(":spotlessApply"))
}