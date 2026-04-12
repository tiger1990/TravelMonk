plugins {
    alias(libs.plugins.travelmonk.android.feature)
}

android {
    namespace = "com.travelmonk.feature.services"
}

dependencies {
    implementation(project(":feature:services-api"))
    implementation(project(":feature:bookings-api"))  // ServiceNavigator navigates to BookingNavKey.Confirmation
}
