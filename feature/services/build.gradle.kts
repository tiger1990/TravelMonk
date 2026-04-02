plugins {
    alias(libs.plugins.travelmonk.android.feature)
}

android {
    namespace = "com.travelmonk.feature.services"
}

dependencies {
    implementation(project(":feature:services-api"))
}
