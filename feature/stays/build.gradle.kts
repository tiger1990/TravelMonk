plugins {
    alias(libs.plugins.travelmonk.android.feature)
}

android {
    namespace = "com.travelmonk.feature.stays"
}

dependencies {
    implementation(project(":feature:stays-api"))
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
}
