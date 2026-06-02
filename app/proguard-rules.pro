# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# -------------------------------------------------------
# Firebase / Firestore
# -------------------------------------------------------
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
# Keep model classes annotated with @Keep for Firestore serialization
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}
# Prevent stripping of fields used by Firestore's automatic (de)serialization
-keepclassmembers class * {
    public <init>();
}

# -------------------------------------------------------
# Kotlin coroutines
# -------------------------------------------------------
-keep class kotlinx.coroutines.** { *; }
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# -------------------------------------------------------
# Jetpack Compose
# -------------------------------------------------------
-keep class androidx.compose.** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}
-dontwarn androidx.compose.**

# -------------------------------------------------------
# Gson
# -------------------------------------------------------
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
# Prevent R8 from stripping generic type information
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# -------------------------------------------------------
# OkHttp
# -------------------------------------------------------
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep class okio.** { *; }

# -------------------------------------------------------
# OpenAI SDK (com.aallam.openai)
# -------------------------------------------------------
-keep class com.aallam.openai.** { *; }
-dontwarn com.aallam.openai.**

# -------------------------------------------------------
# Google Generative AI / Gemini
# -------------------------------------------------------
-keep class com.google.ai.client.generativeai.** { *; }
-dontwarn com.google.ai.client.generativeai.**

# -------------------------------------------------------
# App data/model packages — keep field names for Firestore serialization
# -------------------------------------------------------
-keep class com.nextgenbuildpro.**.data.model.** { *; }
-keepclassmembers class com.nextgenbuildpro.**.data.model.** {
    public <init>();
    <fields>;
}
-keep class com.nextgenbuildpro.**.data.repository.** { *; }
-keepclassmembers class com.nextgenbuildpro.**.data.repository.** {
    public <init>();
    <fields>;
}