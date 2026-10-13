import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.stream.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "io.getstream.chat.android.ai.ondevice"

    defaultConfig {
        // ML Kit's GenAI APIs need Android 8.0.
        minSdk = 26
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

    // Flow is part of this module's API.
    api(libs.kotlinx.coroutines.core)
    implementation(libs.mlkit.genai.prompt)

    testImplementation(libs.junit)
}
