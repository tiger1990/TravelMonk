plugins {
    alias(libs.plugins.travelmonk.android.feature)
}

android {
    namespace = "com.travelmonk.feature.experiences"
}

dependencies {
    implementation(project(":feature:experiences-api"))
    implementation(project(":feature:bookings-api"))  // ExperienceNavigator navigates to BookingNavKey.Confirmation
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    testImplementation(project(":core:testing"))
    testImplementation(libs.junit4)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
}
