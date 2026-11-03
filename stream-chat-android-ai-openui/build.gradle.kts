import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.stream.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.android.junit5)
}

apply(from = "$rootDir/scripts/detekt-compose.gradle")

android {
    namespace = "io.getstream.chat.android.ai.openui"

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

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
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
        // Images load with the shared loader of stream-chat-android-ai-compose.
        optIn.add("io.getstream.chat.android.ai.compose.util.internal.InternalStreamAIApi")
    }
}

dependencies {
    detektPlugins(libs.detekt.formatting)

    // The generative UI layer this module plugs OpenUI into.
    api(project(":stream-chat-android-ai-compose"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    // Material icons stand in for the lucide icons OpenUI names; the ones missing from core are
    // vendored in the icons package.
    implementation(libs.androidx.compose.material.icons.core)
    // The Markdown renderer stream-chat-android-ai-compose uses.
    implementation(libs.markdown)
    implementation(libs.markdown.renderer)
    implementation(libs.markdown.renderer.m3)

    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.kotlinx.coroutines.test)
    // The JVM implementation of org.json, which android.jar only stubs in unit tests.
    testImplementation(libs.org.json)
    testRuntimeOnly(libs.junit.jupiter.engine)
    // Compose UI tests run on Robolectric under JUnit 4.
    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.junit)
    testImplementation(libs.robolectric)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testRuntimeOnly(libs.junit.vintage.engine)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
