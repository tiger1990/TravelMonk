plugins {
    alias(libs.plugins.travelmonk.android.library)
    alias(libs.plugins.travelmonk.android.library.compose)
}

android {
    namespace = "com.travelmonk.core.design.system"
}

dependencies {
    implementation(libs.androidx.core.ktx)
}
