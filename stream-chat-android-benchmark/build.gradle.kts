import io.getstream.chat.android.Configuration
import io.getstream.chat.android.Dependencies

plugins {
    id("com.android.test")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "io.getstream.chat.android.benchmark"
    compileSdk = Configuration.compileSdk

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    defaultConfig {
        minSdk = 24
        targetSdk = Configuration.targetSdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        // This benchmark buildType is used for benchmarking, and should function like your
        // release build (for example, with minification on). It"s signed with a debug key
        // for easy local/CI testing.
        create("benchmark") {
            isDebuggable = true
            signingConfig = getByName("debug").signingConfig
            matchingFallbacks += listOf("release")
        }
    }

    targetProjectPath = ":stream-chat-android-compose-sample"
    experimentalProperties["android.experimental.self-instrumenting"] = true
}

dependencies {
    implementation(Dependencies.androidxTestRunner)
    implementation(Dependencies.baseProfile)
    implementation(Dependencies.macroBenchmark)
    implementation(Dependencies.androidxUiAutomator)
    detektPlugins(Dependencies.detektFormatting)
}

androidComponents {
    beforeVariants(selector().all()) {
        it.enable = it.buildType == "benchmark"
    }
}
