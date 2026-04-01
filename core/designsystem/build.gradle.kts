plugins {
    alias(libs.plugins.travelmonk.android.library)
    alias(libs.plugins.travelmonk.android.library.compose)
}

android {
    namespace = "com.travelmonk.core.designsystem"
}

dependencies {
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.core)
}
