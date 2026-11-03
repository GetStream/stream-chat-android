import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.stream.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "io.getstream.chat.android.ai.a2ui"

    defaultConfig {
        minSdk = libs.versions.aiComposeMinSdk.get().toInt()
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll(
            listOf(
                "-progressive",
                "-Xconsistent-data-class-copy-visibility",
                "-Xexplicit-api=strict",
            ),
        )
    }
}

dependencies {
    detektPlugins(libs.detekt.formatting)

    // The generative UI layer this module plugs A2UI into.
    api(project(":stream-chat-android-ai-compose"))

    testImplementation(libs.junit)
}
