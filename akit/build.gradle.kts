import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.internal.os.OperatingSystem

plugins {
    id("cpp")
    id("java")
    id("jacoco")
    id("google-test")
    id("edu.wpi.first.wpilib.repositories.WPILibRepositoriesPlugin") version "2025.0"
    id("edu.wpi.first.NativeUtils") version "2026.0.1"
    id("edu.wpi.first.GradleJni") version "1.1.0"
    id("edu.wpi.first.GradleVsCode") version "2.1.0"
    id("com.diffplug.spotless") version "6.25.0"
}

sourceSets["main"].java {
    srcDir("src/main/thirdparty/java")
    srcDir("src/main/generated/java")
}

repositories {
    mavenCentral()
}
wpilibRepositories.addAllReleaseRepositories(project)

val wpilibVersion = "2026.1.1"

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
    implementation("edu.wpi.first.thirdparty.frc2025.opencv:opencv-java:4.10.0-3")
    implementation("us.hebi.quickbuf:quickbuf-runtime:1.3.3")


    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Javadoc> {
    exclude("com/google/flatbuffers/**", "org/littletonrobotics/conduit/**")
    title = "AdvantageKit API"
    options {
        (this as StandardJavadocDocletOptions).apply {
            links("https://docs.oracle.com/en/java/javase/17/docs/api/", "https://github.wpilib.org/allwpilib/docs/release/java/")
            stylesheetFile = file("javadoc.css")
            addBooleanOption("Werror", true)
        }
    }
}

// Determine the NativeUtils platform classifier for the current OS/arch so we
// can locate the WPI native libraries that were extracted by the C++ build.
val wpilibNativePlatform: String by lazy {
    val os = OperatingSystem.current()
    when {
        os.isMacOsX -> "osxuniversal"
        os.isLinux -> {
            val arch = System.getProperty("os.arch") ?: "amd64"
            if (arch.contains("aarch64") || arch.contains("arm64")) "linuxarm64"
            else if (arch.contains("arm")) "linuxarm32"
            else "linuxx86-64"
        }
        os.isWindows -> "windowsx86-64"
        else -> throw GradleException("Unsupported platform for WPI native library detection")
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    finalizedBy(tasks.named("jacocoTestReport"))

    // The C++ install task extracts all WPI shared libraries (libwpiHaljni, libwpiutil, etc.)
    // into build/install/wpilibioTest/<platform>/release/lib/. Depend on that task so the
    // libraries exist before the JVM test process starts, then add the directory to
    // java.library.path so RuntimeLoader can find them without extracting from JARs.
    //
    // NativeUtils creates the install tasks via the old Gradle software model, so they are
    // not available via tasks.named() at configuration time. Use tasks.matching() instead
    // (which is lazy and resolves after the model is realised).
    val installTaskName =
        "installWpilibioTest${wpilibNativePlatform.replaceFirstChar { it.uppercaseChar() }}ReleaseGoogleTestExe"
    dependsOn(tasks.matching { it.name == installTaskName })

    val wpiNativeLibDir =
        layout.buildDirectory.dir("install/wpilibioTest/$wpilibNativePlatform/release/lib")
    val wpilibioSharedLibDir =
        layout.buildDirectory.dir("libs/wpilibio/shared/$wpilibNativePlatform/release")

    doFirst {
        jvmArgs(
            "-Djava.library.path=${wpiNativeLibDir.get().asFile.absolutePath}${File.pathSeparator}${wpilibioSharedLibDir.get().asFile.absolutePath}"
        )
    }
}

tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.named("test"))
    reports {
        xml.required.set(false)
        html.required.set(true)
        csv.required.set(true)
    }
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
