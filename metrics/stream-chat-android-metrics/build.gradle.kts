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
        create("stream-chat-android-ai-compose-baseline") {
            dimension = "sdk"
            minSdk = libs.versions.aiComposeMinSdk.get().toInt()
        }
        create("stream-chat-android-ai-compose-stream") {
            dimension = "sdk"
            minSdk = libs.versions.aiComposeMinSdk.get().toInt()
        }
        create("stream-chat-android-ai-ondevice-baseline") {
            dimension = "sdk"
            minSdk = libs.versions.aiOnDeviceMinSdk.get().toInt()
        }
        create("stream-chat-android-ai-ondevice-stream") {
            dimension = "sdk"
            minSdk = libs.versions.aiOnDeviceMinSdk.get().toInt()
        }
        create("stream-chat-android-ai-a2ui-baseline") {
            dimension = "sdk"
            minSdk = libs.versions.aiComposeMinSdk.get().toInt()
        }
        create("stream-chat-android-ai-a2ui-stream") {
            dimension = "sdk"
            minSdk = libs.versions.aiComposeMinSdk.get().toInt()
        }
        create("stream-chat-android-ai-openui-baseline") {
            dimension = "sdk"
            minSdk = libs.versions.aiComposeMinSdk.get().toInt()
        }
        create("stream-chat-android-ai-openui-stream") {
            dimension = "sdk"
            minSdk = libs.versions.aiComposeMinSdk.get().toInt()
        }
    }
}

afterEvaluate {
    android.productFlavors.forEach { flavor ->
        val flavorName = flavor.name
        // For compose flavors, we apply the compose plugin,
        // set up build features and add common compose dependencies.
        // The ai-a2ui and ai-openui flavors are Compose too: both render through ai-compose.
        if (flavorName.contains("compose") ||
            flavorName.startsWith("stream-chat-android-ai-a2ui") ||
            flavorName.startsWith("stream-chat-android-ai-openui")
        ) {
            val composePlugin = libs.plugins.kotlin.compose.get()
            plugins.apply(composePlugin.pluginId)
            android.buildFeatures.compose = true
            val configurationName = "${flavorName}Implementation"
            dependencies.add(configurationName, dependencies.platform(libs.androidx.compose.bom))
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

    "stream-chat-android-ai-compose-streamImplementation"(project(":stream-chat-android-ai-compose"))

    "stream-chat-android-ai-ondevice-streamImplementation"(project(":stream-chat-android-ai-ondevice"))

    // The baseline carries ai-compose, so the number is the cost of A2UI on top of it.
    "stream-chat-android-ai-a2ui-baselineImplementation"(project(":stream-chat-android-ai-compose"))
    "stream-chat-android-ai-a2ui-streamImplementation"(project(":stream-chat-android-ai-a2ui"))
    // The ai-openui baseline includes ai-compose so the reported size is ai-openui's own cost.
    "stream-chat-android-ai-openui-baselineImplementation"(project(":stream-chat-android-ai-compose"))
    "stream-chat-android-ai-openui-streamImplementation"(project(":stream-chat-android-ai-openui"))
}
