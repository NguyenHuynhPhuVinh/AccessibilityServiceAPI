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

# Keep Android system classes for keyboard actions
-keep class android.app.Instrumentation { *; }
-keep class android.view.KeyEvent { *; }
-keep class android.view.inputmethod.** { *; }
-keep class android.accessibilityservice.AccessibilityService { *; }
-keep class android.view.accessibility.AccessibilityNodeInfo { *; }

# Keep methods used for keyboard actions
-keepclassmembers class * {
    public void sendKeyDownUpSync(android.view.KeyEvent);
    public boolean performAction(int);
    public boolean performAction(int, android.os.Bundle);
}

# Prevent obfuscation of reflection-based calls
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Don't obfuscate system services and managers
-keep class * extends android.app.Service { *; }
-keep class * extends android.accessibilityservice.AccessibilityService { *; }

# Keep constants that might be used via reflection
-keepclassmembers class * {
    static final int ACTION_*;
    static final int KEYCODE_*;
    static final int GLOBAL_ACTION_*;
}

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