plugins {
    alias(libs.plugins.travelmonk.android.feature)
}

android {
    namespace = "com.travelmonk.feature.stays"
}

dependencies {
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
}
