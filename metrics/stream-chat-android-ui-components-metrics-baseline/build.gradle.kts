import io.getstream.chat.android.Dependencies

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

apply(from = "$rootDir/scripts/android.gradle")

android {
    namespace = "io.getstream.chat.android.ui.components.metrics.baseline"
    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("debug")
        }
    }
}

dependencies {
    detektPlugins(Dependencies.detektFormatting)

    implementation(Dependencies.coroutinesCore)
    implementation(Dependencies.androidxAppCompat)
}
