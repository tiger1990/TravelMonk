plugins {
    alias(libs.plugins.travelmonk.android.library)
    alias(libs.plugins.travelmonk.android.library.compose)
    alias(libs.plugins.travelmonk.android.hilt)
}

android {
    namespace = "com.travelmonk.core.common"
}

dependencies {
    // ...existing code...
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.coroutines.core)
}
