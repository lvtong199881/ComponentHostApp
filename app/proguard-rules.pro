# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /sdk/tools/proguard/proguard-android.txt

# Keep React Native
-keep class com.facebook.react.** { *; }
-keep class com.facebook.soloader.** { *; }

# Keep native methods
-keepclassmembers class * {
    native <methods>;
}
