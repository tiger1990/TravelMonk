plugins {
    alias(libs.plugins.travelmonk.android.library)
}

android {
    namespace = "com.travelmonk.core.navigation"
}

dependencies {
    api(libs.androidx.navigation3.runtime)
}
