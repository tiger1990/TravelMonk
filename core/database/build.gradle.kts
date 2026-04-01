plugins {
    alias(libs.plugins.travelmonk.android.library)
    alias(libs.plugins.travelmonk.android.hilt)
}

android {
    namespace = "com.travelmonk.core.database"
}

dependencies {
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
}
