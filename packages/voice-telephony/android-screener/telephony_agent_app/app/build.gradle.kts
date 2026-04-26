import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.example.telephonyagent"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.telephonyagent"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "0.1"

        // Configure native code for ONNX Runtime integration
        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17"
            }
        }
        ndk {
            // Support common ABIs for most devices
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86_64")
        }
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.7.6"
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            // Exclude duplicate license files
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt"
            )
        }
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.18.1"
        }
    }
}

dependencies {
    // Kotlin and AndroidX
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.core:core-telecom:1.0.0-alpha02")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-service:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.0")

    // Jetpack Compose
    val composeVersion = "1.7.6"
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-test-manifest:$composeVersion")

    // Material icons for Compose (used in bottom navigation)
    implementation("androidx.compose.material:material-icons-extended:$composeVersion")

    // Telephony and WorkManager
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("androidx.biometric:biometric:1.2.0-alpha05")

    // ONNX Runtime and AI processing
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.20.1")
    implementation("com.microsoft.onnxruntime:onnxruntime-genai:0.5.1")
    implementation("ai.onnxruntime:onnxruntime-extensions-android:0.12.0")

    // Audio and media processing
    implementation("androidx.media3:media3-exoplayer:1.5.0")
    implementation("androidx.media3:media3-session:1.5.0")
    implementation("org.xiph:opus-android:1.0.0")
    implementation("com.github.luben:zstd-jni:1.5.6-8")

    // Speech recognition and gRPC
    implementation("com.google.cloud:google-cloud-speech:4.44.0")
    implementation("io.grpc:grpc-okhttp:1.68.1")

    // Networking and JSON
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.google.code.gson:gson:2.11.0")

    // Room database with SQLCipher encryption
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("net.zetetic:android-database-sqlcipher:4.6.1")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.3")

    // Android security for encrypted shared preferences
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Hypothetical Deepgram and ElevenLabs SDKs (define your own interfaces if unavailable)
    implementation("com.deepgram:deepgram-sdk:0.11.0")
    implementation("com.elevenlabs:elevenlabs-sdk:0.3.0")
}