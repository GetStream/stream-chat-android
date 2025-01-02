import io.getstream.chat.android.Configuration

plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.androidx.baseline.profile)
}

apply {
    from("$rootDir/scripts/android.gradle")
}

android {
    namespace = "io.getstream.chat.android.benchmark"
    compileSdk = Configuration.compileSdk

    defaultConfig {
        minSdk = 24
        targetSdk = Configuration.targetSdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "EMULATOR"
        buildConfigField("String", "STREAM_CHAT_VERSION", "\"$version\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        buildConfig = true
    }

    targetProjectPath = ":stream-chat-android-compose-sample"

    testOptions.managedDevices.devices {
        maybeCreate<com.android.build.api.dsl.ManagedVirtualDevice>("pixel6api31").apply {
            device = "Pixel 6"
            apiLevel = 31
            systemImageSource = "aosp"
        }
    }
}

// This is the plugin configuration. Everything is optional. Defaults are in the
// comments. In this example, you use the GMD added earlier and disable connected devices.
baselineProfile {

    // This specifies the managed devices to use that you run the tests on. The default
    // is none.
    managedDevices += "pixel6api31"

    // This enables using connected devices to generate profiles. The default is true.
    // When using connected devices, they must be rooted or API 33 and higher.
    useConnectedDevices = false
}

dependencies {
    implementation(libs.androidx.test.runner)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.benchmark.macro.junit4)
    implementation(libs.androidx.test.uiautomator)
    detektPlugins(libs.detekt.formatting)
}
