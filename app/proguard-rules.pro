# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep the data classes for serialization
-keep class com.luckypaneler.analyzer.** { *; }
-keep class com.luckypaneler.processing.** { *; }
