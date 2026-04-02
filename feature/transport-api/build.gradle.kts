plugins {
    alias(libs.plugins.travelmonk.android.library)
    alias(libs.plugins.travelmonk.android.library.compose)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

android {
    namespace = "com.travelmonk.feature.transportapi"
}

dependencies {
    api(project(":core:navigation"))
    implementation(libs.kotlinx.serialization.core)
}
