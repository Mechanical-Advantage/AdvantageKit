plugins {
    id("cpp")
    id("java")
    id("google-test")
    id("edu.wpi.first.wpilib.repositories.WPILibRepositoriesPlugin") version "2020.2"
    id("edu.wpi.first.NativeUtils") version "2025.3.0"
    id("edu.wpi.first.GradleJni") version "1.1.0"
    id("edu.wpi.first.GradleVsCode") version "2.1.0"
}

sourceSets["main"].java {
    srcDir("src/main/java")
    srcDir("src/main/thirdparty/java")
    srcDir("src/main/generated/java")
}

repositories {
    mavenCentral()
}
if (project.hasProperty("releaseMode")) {
    wpilibRepositories.addAllReleaseRepositories(project)
} else {
    wpilibRepositories.addAllDevelopmentRepositories(project)
}

dependencies {
    implementation("edu.wpi.first.cscore:cscore-java:2025.+")
    implementation("edu.wpi.first.cameraserver:cameraserver-java:2025.+")
    implementation("edu.wpi.first.ntcore:ntcore-java:2025.+")
    implementation("edu.wpi.first.wpilibj:wpilibj-java:2025.+")
    implementation("edu.wpi.first.wpiutil:wpiutil-java:2025.+")
    implementation("edu.wpi.first.wpimath:wpimath-java:2025.+")
    implementation("edu.wpi.first.wpiunits:wpiunits-java:2025.+")
    implementation("edu.wpi.first.hal:hal-java:2025.+")
    implementation("org.ejml:ejml-simple:0.43.1")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.15.2")
    implementation("com.fasterxml.jackson.core:jackson-core:2.15.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("edu.wpi.first.thirdparty.frc2024.opencv:opencv-java:4.8.0-4")

    implementation("us.hebi.quickbuf:quickbuf-runtime:1.3.2")
}

apply(from = "native.gradle")
