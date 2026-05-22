plugins {
    alias(libs.plugins.travelmonk.android.library)
    alias(libs.plugins.travelmonk.android.hilt)
}

android {
    namespace = "com.travelmonk.core.testing"
}

dependencies {
    api(project(":core:common"))
    api(libs.hilt.android.testing)
    api(libs.junit4)
    api(libs.turbine)
    api(libs.kotlinx.coroutines.test)
    api(libs.mockk)
}
