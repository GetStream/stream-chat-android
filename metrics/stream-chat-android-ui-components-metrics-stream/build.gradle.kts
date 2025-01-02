plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

apply(from = "$rootDir/scripts/android.gradle")

android {
    namespace = "io.getstream.chat.android.ui.components.metrics.stream"
    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("debug")
        }
    }
}

dependencies {
    detektPlugins(libs.detekt.formatting)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.appcompat)

    implementation(project(":stream-chat-android-ui-components"))
}
