plugins {
    alias(libs.plugins.stream.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "io.getstream.chat.android.metrics"
    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("debug")
        }
    }

    flavorDimensions += "sdk"

    productFlavors {
        create("stream-chat-android-client-baseline") {
            dimension = "sdk"
        }
        create("stream-chat-android-client-stream") {
            dimension = "sdk"
        }
        create("stream-chat-android-compose-baseline") {
            dimension = "sdk"
        }
        create("stream-chat-android-compose-stream") {
            dimension = "sdk"
        }
        create("stream-chat-android-ui-components-baseline") {
            dimension = "sdk"
        }
        create("stream-chat-android-ui-components-stream") {
            dimension = "sdk"
        }
    }
}

afterEvaluate {
    android.productFlavors.forEach { flavor ->
        val flavorName = flavor.name
        // For compose flavors, we apply the compose plugin,
        // set up build features and add common compose dependencies.
        if (flavorName.contains("compose")) {
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

    "stream-chat-android-client-streamImplementation"(project(":stream-chat-android-client"))

    "stream-chat-android-ui-components-streamImplementation"(project(":stream-chat-android-ui-components"))

    "stream-chat-android-compose-streamImplementation"(project(":stream-chat-android-compose"))
}
