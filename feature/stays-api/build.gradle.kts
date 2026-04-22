plugins {
    alias(libs.plugins.travelmonk.android.library)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

android {
    namespace = "com.travelmonk.feature.staysapi"
}

dependencies {
    api(project(":core:navigation"))
    api(project(":core:model"))
    implementation(libs.kotlinx.serialization.core)
}
