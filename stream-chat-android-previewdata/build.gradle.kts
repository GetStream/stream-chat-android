import io.getstream.chat.android.Configuration
import io.getstream.chat.android.Dependencies

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

rootProject.extra.apply {
    set("PUBLISH_GROUP_ID", Configuration.artifactGroup)
    set("PUBLISH_ARTIFACT_ID", "stream-video-android-previewdata")
    set("PUBLISH_VERSION", rootProject.extra.get("rootVersionName"))
}

apply(from = "$rootDir/scripts/publish-module.gradle")
apply(from = "$rootDir/scripts/android.gradle")

android {
    namespace = "io.getstream.chat.android.previewdata"
    resourcePrefix = "stream_compose_previewdata"

    buildFeatures {
        compose = true
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.freeCompilerArgs.addAll(
        listOf(
            "-Xexplicit-api=strict",
        ),
    )
}

dependencies {
    implementation(project(":stream-chat-android-state"))
    implementation(project(":stream-chat-android-client"))

    // Compose
    implementation(Dependencies.composeUi)

    detektPlugins(Dependencies.detektFormatting)
}
