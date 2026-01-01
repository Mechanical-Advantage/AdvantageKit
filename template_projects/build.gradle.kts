plugins {
    id("java")
    id("com.diffplug.spotless") version "6.25.0"
}

repositories {
    mavenCentral()
}

spotless {
    java {
        target("sources/**/*.java")
        toggleOffOn()
        googleJavaFormat()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
        licenseHeader("// Copyright (c) 2021-2026 Littleton Robotics\n// http://github.com/Mechanical-Advantage\n//\n// Use of this source code is governed by a BSD\n// license that can be found in the LICENSE file\n// at the root directory of this project.\n\n")
    }
}