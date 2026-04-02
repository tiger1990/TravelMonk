import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.travelmonk.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    implementation(libs.kotlin.serialization.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "travelmonk.android.library"
            implementationClass = "com.travelmonk.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = "travelmonk.android.library.compose"
            implementationClass = "com.travelmonk.buildlogic.AndroidLibraryComposeConventionPlugin"
        }
        register("androidFeature") {
            id = "travelmonk.android.feature"
            implementationClass = "com.travelmonk.buildlogic.AndroidFeatureConventionPlugin"
        }
        register("androidHilt") {
            id = "travelmonk.android.hilt"
            implementationClass = "com.travelmonk.buildlogic.AndroidHiltConventionPlugin"
        }
    }
}
