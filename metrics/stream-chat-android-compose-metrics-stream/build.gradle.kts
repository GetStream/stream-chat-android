import io.getstream.chat.android.Dependencies

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

apply(from = "$rootDir/scripts/android.gradle")

android {
    namespace = "io.getstream.chat.android.compose.metrics.stream"
    buildFeatures.compose = true
    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("debug")
        }
    }
}

dependencies {
    detektPlugins(Dependencies.detektFormatting)

    implementation(Dependencies.composeUi)
    implementation(Dependencies.composeUiTooling)
    implementation(Dependencies.composeFoundation)
    implementation(Dependencies.composeMaterial)
    implementation(Dependencies.composeMaterialIcons)

    implementation(Dependencies.composeActivity)
    implementation(Dependencies.composeAndroidLifecycle)
    implementation(Dependencies.composeViewModel)
    implementation(Dependencies.composeAccompanistPermissions)
    implementation(Dependencies.composeAccompanistPager)

    implementation(project(":stream-chat-android-compose"))
}
