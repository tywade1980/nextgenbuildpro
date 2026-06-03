plugins {
    id("com.android.application") version "8.4.1" apply false
    id("org.jetbrains.kotlin.android") version "2.2.2" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}