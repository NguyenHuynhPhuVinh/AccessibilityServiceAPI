# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Accessibility Service classes
-keep class com.tomisakae.accessibilityserviceapi.infrastructure.accessibility.** { *; }
-keep class com.tomisakae.accessibilityserviceapi.AccessibilityServiceAPI { *; }

# Keep HTTP server classes
-keep class com.tomisakae.accessibilityserviceapi.infrastructure.http.** { *; }
-keep class fi.iki.elonen.** { *; }

# Keep data models for JSON serialization
-keep class com.tomisakae.accessibilityserviceapi.domain.models.** { *; }

# Keep Gson classes
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.flow.**

# Keep line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# General Android optimizations
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose