plugins {
    alias(libs.plugins.travelmonk.android.library)
    alias(libs.plugins.travelmonk.android.library.compose)
}

android {
    namespace = "com.travelmonk.core.ui"
}

dependencies {
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
}
