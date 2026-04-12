plugins {
    alias(libs.plugins.travelmonk.android.feature)
}

android {
    namespace = "com.travelmonk.feature.home"
}

dependencies {
    implementation(project(":feature:home-api"))
    implementation(project(":feature:transport-api")) // HomeNavigator navigates to TransportNavKey.Root
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
}
