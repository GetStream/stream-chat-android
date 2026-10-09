plugins {
    alias(libs.plugins.stream.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

apply(from = "$rootDir/scripts/detekt-compose.gradle")

android {
    namespace = "io.getstream.chat.android.ai.compose.sample"

    defaultConfig {
        applicationId = "io.getstream.chat.android.ai.compose.sample"
        // The on-device model needs Android 8.0.
        minSdk = 26
        targetSdk = libs.versions.sampleTargetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    detektPlugins(libs.detekt.formatting)

    implementation(project(":stream-chat-android-ai-compose"))
    implementation(project(":stream-chat-android-ai-ondevice"))

    implementation(project(":stream-chat-android-client"))
    implementation(project(":stream-chat-android-compose"))
    implementation(libs.stream.log)

    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)

    testImplementation(libs.junit)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
