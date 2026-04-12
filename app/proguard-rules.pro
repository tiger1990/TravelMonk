# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ── Crash Reporting ───────────────────────────────────────────────────────────
# Preserve source file names and line numbers so stack traces in Firebase
# Crashlytics / Play Console are human-readable.
# R8 still obfuscates class/method names — the mapping.txt file maps them back.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
