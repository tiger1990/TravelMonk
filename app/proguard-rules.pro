# ══════════════════════════════════════════════════════════════════════════════
# TravelMonk — ProGuard/R8 Rules
# ══════════════════════════════════════════════════════════════════════════════

# ── Metadata ──────────────────────────────────────────────────────────────────
# Preserve source file names and line numbers for readable crash reports.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Navigation3 (Process Death Recovery) ──────────────────────────────────────
# Navigation3 uses Fully Qualified Class Names (FQCN) to restore the backstack
# after process death. If these classes are renamed or stripped, the app will
# fail to restore state (silently dropping users to the home screen).
-keep class * implements androidx.navigation3.runtime.NavKey { *; }
-keepnames class * implements androidx.navigation3.runtime.NavKey

# ── Hilt ─────────────────────────────────────────────────────────────────────
# Most Hilt rules are provided automatically by Hilt's own consumer AAR rules.
#
# NOTE: For custom @EntryPoint interfaces accessed via EntryPointAccessors
# (reflection), do NOT add global rules here. Instead, annotate the interface
# in the source code with @androidx.annotation.Keep.
#
# This allows R8 to still optimize/strip EntryPoints that are NOT accessed
# reflectively, resulting in a smaller APK.
-keepattributes *Annotation*

# ── Kotlin Serialization ──────────────────────────────────────────────────────
# Keep serializable names for any classes used with kotlinx.serialization
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
