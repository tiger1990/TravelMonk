plugins {
    alias(libs.plugins.travelmonk.android.feature)
}

android {
    namespace = "com.travelmonk.feature.transport"
}

dependencies {
    implementation(project(":feature:transport-api"))
    // TransportNavKey is now in :feature:transport-api (via api() transitive)
}
