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

# ── OkHttp ────────────────────────────────────────────────────────────────────
# Suppress warnings for optional platform integrations OkHttp probes at runtime.
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
