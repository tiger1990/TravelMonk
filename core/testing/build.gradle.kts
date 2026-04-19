plugins {
    alias(libs.plugins.travelmonk.android.library)
}

android {
    namespace = "com.travelmonk.core.testing"
}

dependencies {
    api(project(":core:common"))
    api(libs.junit4)
    api(libs.turbine)
    api(libs.kotlinx.coroutines.test)
    api(libs.mockk)
}