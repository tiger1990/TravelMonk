plugins {
    alias(libs.plugins.travelmonk.android.library)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

android {
    namespace = "com.travelmonk.core.model"
}

dependencies {
    // Serialization for shared data models consumed across features and network layer
    implementation(libs.kotlinx.serialization.core)
}
