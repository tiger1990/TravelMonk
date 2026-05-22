plugins {
    alias(libs.plugins.travelmonk.android.feature)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

android {
    namespace = "com.travelmonk.feature.onboarding"
}

dependencies {
    implementation(project(":feature:onboarding-api"))
    implementation(project(":core:common"))

    // Encrypted session storage: proto-style DataStore + Tink AEAD
    implementation(libs.androidx.datastore.core)
    implementation(libs.tink.android)

    // Credential Manager (Passkeys)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    testImplementation(project(":core:testing"))
    testImplementation(libs.junit4)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
}
