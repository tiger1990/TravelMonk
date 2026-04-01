plugins {
    alias(libs.plugins.travelmonk.android.feature)
}

android {
    namespace = "com.travelmonk.feature.experiences"
}

dependencies {
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
}
