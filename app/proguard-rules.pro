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

# =========================================================================
# Thales/Gemalto FIDO2 SDK ProGuard & R8 Rules
# =========================================================================

# Suppress warnings and keep public APIs for com.thalesgroup.gemalto.fido2
-dontwarn com.thalesgroup.gemalto.fido2.**
-keep class com.thalesgroup.gemalto.fido2.** { *; }

# Suppress warnings and keep public APIs for other Thales/Gemalto packages
-dontwarn com.thalesgroup.gemalto.**
-keep public class com.thalesgroup.gemalto.** {
    public protected *;
}
-keepclassmembers class com.thalesgroup.gemalto.** {
    public protected *;
}

# Suppress warnings and keep util.x.** (Thales Protector FIDO SDK internal packages)
-dontwarn util.x.**
-dontnote util.x.**
-keep,allowobfuscation class util.x.** { *; }

# Suppress warnings and keep Java Native Access (JNA) dependencies used by the SDK
-dontwarn java.awt.*
-keep class com.sun.jna.** { *; }

# Suppress warning for missing FingerprintManager in newer compile SDK and R8 environments
-dontwarn android.hardware.fingerprint.FingerprintManager

# Keep generic signatures for Retrofit and Kotlin Coroutines
-keepattributes Signature
-keepattributes *Annotation*

# Prevent ProGuard from obfuscating our Showcase App API Data Models
-keep class com.onewelcome.core.network.api.** { *; }