plugins {
    alias(libs.plugins.travelmonk.android.feature)
}

android {
    namespace = "com.travelmonk.feature.flights"
}

dependencies {
    implementation(project(":feature:flights-api"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
}
