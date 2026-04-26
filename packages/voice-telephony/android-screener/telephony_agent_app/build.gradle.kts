// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    // Apply the Android Application plugin and the Kotlin Android plugin in submodules only.
    // Versions are defined here for convenience.
    id("com.android.application") version "8.4.1" apply false
    id("org.jetbrains.kotlin.android") version "2.2.2" apply false
    id("org.jetbrains.kotlin.kapt") version "2.2.2" apply false
}

allprojects {
    // Ensure reproducible builds by using canonical repository order.
    repositories {
        google()
        mavenCentral()
    }
}