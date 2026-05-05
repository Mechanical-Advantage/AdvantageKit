import org.gradle.external.javadoc.StandardJavadocDocletOptions

plugins {
    id("cpp")
    id("java")
    id("google-test")
    id("org.wpilib.WPILibRepositoriesPlugin") version "2027.0.0"
    id("org.wpilib.NativeUtils") version "2027.1.1"
    id("org.wpilib.GradleJni") version "2027.0.0"
    id("org.wpilib.GradleVsCode") version "2027.0.0"
    id("com.diffplug.spotless") version "6.25.0"
}

sourceSets["main"].java {
    srcDir("src/main/thirdparty/java")
    srcDir("src/main/generated/java")
}

repositories {
    mavenCentral()
}
wpilibRepositories.use2027Repos()
wpilibRepositories.addAllReleaseRepositories(project)

val wpilibVersion = "2027.0.0-alpha-5"

dependencies {
    implementation("org.wpilib.cscore:cscore-java:$wpilibVersion")
    implementation("org.wpilib.cameraserver:cameraserver-java:$wpilibVersion")
    implementation("org.wpilib.ntcore:ntcore-java:$wpilibVersion")
    implementation("org.wpilib.wpilibj:wpilibj-java:$wpilibVersion")
    implementation("org.wpilib.wpiutil:wpiutil-java:$wpilibVersion")
    implementation("org.wpilib.wpimath:wpimath-java:$wpilibVersion")
    implementation("org.wpilib.wpiunits:wpiunits-java:$wpilibVersion")
    implementation("org.wpilib.datalog:datalog-java:$wpilibVersion")
    implementation("org.wpilib.hal:hal-java:$wpilibVersion")
    implementation("org.ejml:ejml-simple:0.43.1")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.15.2")
    implementation("com.fasterxml.jackson.core:jackson-core:2.15.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("edu.wpi.first.thirdparty.frc2025.opencv:opencv-java:4.10.0-3")
    implementation("us.hebi.quickbuf:quickbuf-runtime:1.3.3")


    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Javadoc> {
    exclude("com/google/flatbuffers/**", "org/littletonrobotics/conduit/**")
    title = "AdvantageKit API"
    options {
        (this as StandardJavadocDocletOptions).links("https://docs.oracle.com/en/java/javase/25/docs/api/", "https://github.wpilib.org/allwpilib/docs/release/java/")
        (this as StandardJavadocDocletOptions).stylesheetFile = file("javadoc.css")
        (this as StandardJavadocDocletOptions).addBooleanOption("Werror", true)
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

java {
    withSourcesJar()
    withJavadocJar()
}

// Spotless formatting
spotless {
    java {
        target("src/main/java/**/*.java", "src/test/java/**/*.java", "autolog/src/main/java/**/*.java")
        toggleOffOn()
        googleJavaFormat()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
        licenseHeader("// Copyright (c) 2021-2026 Littleton Robotics\n// http://github.com/Mechanical-Advantage\n//\n// Use of this source code is governed by a BSD\n// license that can be found in the LICENSE file\n// at the root directory of this project.\n\n")
    }
    cpp {
        target("src/main/native/**/*.cc", "src/main/native/**/*.h", "src/test/native/**/*.cc", "src/test/native/**/*.h")
        toggleOffOn()
        eclipseCdt()
        trimTrailingWhitespace()
        endWithNewline()
        licenseHeader("// Copyright (c) 2021-2026 Littleton Robotics\n// http://github.com/Mechanical-Advantage\n//\n// Use of this source code is governed by a BSD\n// license that can be found in the LICENSE file\n// at the root directory of this project.\n\n")
    }
}

apply(from = "native.gradle")
apply(from = "publish.gradle")
