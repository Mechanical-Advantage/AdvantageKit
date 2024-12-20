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
    srcDir("src/main/thirdparty/java")
    srcDir("src/main/generated/java")
}

repositories {
    mavenCentral()
}
wpilibRepositories.addAllReleaseRepositories(project)

val wpilibVersion = "2025.1.1-beta-3"

dependencies {
    implementation("edu.wpi.first.cscore:cscore-java:$wpilibVersion")
    implementation("edu.wpi.first.cameraserver:cameraserver-java:$wpilibVersion")
    implementation("edu.wpi.first.ntcore:ntcore-java:$wpilibVersion")
    implementation("edu.wpi.first.wpilibj:wpilibj-java:$wpilibVersion")
    implementation("edu.wpi.first.wpiutil:wpiutil-java:$wpilibVersion")
    implementation("edu.wpi.first.wpimath:wpimath-java:$wpilibVersion")
    implementation("edu.wpi.first.wpiunits:wpiunits-java:$wpilibVersion")
    implementation("edu.wpi.first.hal:hal-java:$wpilibVersion")
    implementation("org.ejml:ejml-simple:0.43.1")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.15.2")
    implementation("com.fasterxml.jackson.core:jackson-core:2.15.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("edu.wpi.first.thirdparty.frc2024.opencv:opencv-java:4.8.0-4")

    implementation("us.hebi.quickbuf:quickbuf-runtime:1.3.2")
}

tasks.withType<Javadoc> {
    exclude("com/google/flatbuffers/**")
}

java {
    withSourcesJar()
    withJavadocJar()
}

apply(from = "native.gradle")
apply(from = "publish.gradle")