tasks.register<Copy>("generateVsCodeConfig") {
    dependsOn(gradle.includedBuild("akit").task(":generateVsCodeConfig"))

    from(gradle.includedBuild("akit").projectDir.resolve("build/vscodeconfig.json"))
    into(layout.buildDirectory)
}

tasks.register("build") {
    dependsOn(gradle.includedBuild("akit").task(":build"))
    dependsOn(gradle.includedBuild("akit").task(":generateVsCodeConfig"))
}