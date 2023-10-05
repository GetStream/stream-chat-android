import io.getstream.chat.android.Configuration
import io.getstream.chat.android.Dependencies

plugins {
    id("com.android.test")
    id("org.jetbrains.kotlin.android")
    id("androidx.baselineprofile")
}

apply {
    from("$rootDir/scripts/android.gradle")
}

android {
    namespace = "io.getstream.chat.android.benchmark"

    defaultConfig {
        minSdk = 24
        targetSdk = Configuration.targetSdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
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
    implementation(Dependencies.androidxTestRunner)
    implementation(Dependencies.baseProfile)
    implementation(Dependencies.macroBenchmark)
    implementation(Dependencies.androidxUiAutomator)
    detektPlugins(Dependencies.detektFormatting)
}
