plugins {
    alias(libs.plugins.travelmonk.android.library)
    alias(libs.plugins.travelmonk.android.hilt)
}

android {
    namespace = "com.travelmonk.core.network"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:logger"))

    // Exposed via api so consumers (feature modules) can use Retrofit service
    // interfaces and OkHttp types without re-declaring them as direct dependencies.
    api(libs.retrofit)
    api(libs.okhttp)

    implementation(libs.converter.moshi)
    implementation(libs.logging.interceptor)
    implementation(libs.moshi.kotlin)
}
