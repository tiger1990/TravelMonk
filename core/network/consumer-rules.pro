# ── Retrofit ─────────────────────────────────────────────────────────────────
# R8 strips service interfaces because they are only referenced via dynamic proxy.
# This rule keeps every method annotated with a Retrofit HTTP annotation so
# the interface itself is never removed or renamed.
-keep,allowobfuscation,allowshrinking interface * {
    @retrofit2.http.GET <methods>;
    @retrofit2.http.POST <methods>;
    @retrofit2.http.PUT <methods>;
    @retrofit2.http.PATCH <methods>;
    @retrofit2.http.DELETE <methods>;
    @retrofit2.http.HEAD <methods>;
    @retrofit2.http.OPTIONS <methods>;
    @retrofit2.http.HTTP <methods>;
}

# Retrofit uses reflection on suspend functions to detect coroutine continuations.
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# ── Moshi ─────────────────────────────────────────────────────────────────────
# Keep classes annotated with @JsonClass so their generated adapters are not
# stripped. The generated adapter class name is <ClassName>JsonAdapter.
-keep @com.squareup.moshi.JsonClass class * { *; }

# Keep fields annotated with @Json so JSON key names are preserved.
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}

# ── Moshi: KotlinJsonAdapterFactory (reflection adapter) ──────────────────────
# KotlinJsonAdapterFactory reads constructor parameter names via kotlin.reflect.
# R8 must preserve Kotlin metadata and constructor signatures on all model classes
# that Moshi deserializes — i.e. any class returned by a Retrofit @GET/@POST etc.

# Preserve Kotlin metadata annotation (carries parameter names post-compile)
-keep class kotlin.Metadata { *; }
-keepattributes Signature, InnerClasses, EnclosingMethod

# Keep all feature model data classes Moshi deserializes via reflection
# (plain data classes with no @Json / @JsonClass annotations)
-keepclassmembers class com.travelmonk.feature.**.domain.model.** {
    <init>(...);
    <fields>;
}
-keepnames class com.travelmonk.feature.**.domain.model.**

# Keep enum entries used in model classes (Moshi calls values()/valueOf() at runtime)
-keepclassmembers enum com.travelmonk.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    <fields>;
}

# ── OkHttp ────────────────────────────────────────────────────────────────────
# Suppress warnings for optional platform integrations OkHttp probes at runtime.
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
