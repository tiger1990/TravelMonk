plugins {
    alias(libs.plugins.travelmonk.android.library)
    alias(libs.plugins.travelmonk.android.library.compose)
}

android {
    namespace = "com.travelmonk.core.permissions"
}

dependencies {
    // ContextCompat.checkSelfPermission + ActivityCompat.shouldShowRequestPermissionRationale
    implementation(libs.androidx.core.ktx)

    // LocalActivity + rememberLauncherForActivityResult + ActivityResultContracts
    implementation(libs.androidx.activity.compose)

    // TravelMonkTheme tokens used in PermissionRationaleDialog
    implementation(project(":core:design-system"))
}
