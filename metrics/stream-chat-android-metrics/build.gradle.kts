plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

apply(from = "$rootDir/scripts/android.gradle")

android {
    namespace = "io.getstream.chat.android.metrics"
    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("debug")
        }
    }

    flavorDimensions += "sdk"

    productFlavors {
        create("clientBaseline") {
            dimension = "sdk"
        }
        create("clientStream") {
            dimension = "sdk"
        }
        create("offlineBaseline") {
            dimension = "sdk"
        }
        create("offlineStream") {
            dimension = "sdk"
        }
        create("composeBaseline") {
            dimension = "sdk"
        }
        create("composeStream") {
            dimension = "sdk"
        }
        create("uiComponentsBaseline") {
            dimension = "sdk"
        }
        create("uiComponentsStream") {
            dimension = "sdk"
        }
    }
}

afterEvaluate {
    android.productFlavors.forEach { flavor ->
        val flavorName = flavor.name
        // For compose flavors, we apply the compose plugin,
        // set up build features and add common compose dependencies.
        if (flavorName.startsWith("compose")) {
            val composePlugin = libs.plugins.kotlin.compose.get()
            plugins.apply(composePlugin.pluginId)
            android.buildFeatures.compose = true
            val configurationName = "${flavorName}Implementation"
            dependencies.add(configurationName, libs.androidx.compose.ui)
            dependencies.add(configurationName, libs.androidx.compose.ui.tooling)
            dependencies.add(configurationName, libs.androidx.compose.foundation)
            dependencies.add(configurationName, libs.androidx.activity.compose)
            dependencies.add(configurationName, libs.androidx.lifecycle.runtime.compose)
            dependencies.add(configurationName, libs.androidx.lifecycle.viewmodel.compose)
        }
    }
}

dependencies {
    detektPlugins(libs.detekt.formatting)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.appcompat)

    "clientStreamImplementation"(project(":stream-chat-android-client"))

    "offlineStreamImplementation"(project(":stream-chat-android-offline"))

    "uiComponentsStreamImplementation"(project(":stream-chat-android-ui-components"))

    "composeStreamImplementation"(project(":stream-chat-android-compose"))
}
