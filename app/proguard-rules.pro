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

# ── Retrofit & Networking ─────────────────────────────────────────────────────
# Retrofit uses reflection on interface methods and parameters.
# Signature is needed for GSON/Moshi to handle generic types like List<T>.
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**

# ── Moshi ─────────────────────────────────────────────────────────────────────
# Moshi's KotlinJsonAdapter relies on reflection if generated adapters are not used.
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}

# ── Coil (Image Loading) ──────────────────────────────────────────────────────
# Coil 3 includes its own consumer rules. We only need to ensure metadata is
# preserved for its dynamic service loading and reflective features.
-dontwarn coil.**
# Keep the Metadata for Kotlin reflections if you use custom transformations/interceptors
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
