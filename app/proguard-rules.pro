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

-optimizationpasses 5
-allowaccessmodification
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Room, DB & Models
-keep class com.example.db.** { *; }
-keep class com.example.game.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}

# Epic Online Services (EOS) SDK
-keep class com.epicgames.mobile.eossdk.** { *; }
-keepclassmembers class com.epicgames.mobile.eossdk.** { *; }
-keep class com.example.eos.** { *; }
-keepclassmembers class com.example.eos.** { *; }

# AndroidX Security Crypto (required by EOS SDK Keychain)
-keep class androidx.security.crypto.** { *; }
-keepclassmembers class androidx.security.crypto.** { *; }
