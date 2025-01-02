import io.getstream.chat.android.Configuration

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

rootProject.extra.apply {
    set("PUBLISH_GROUP_ID", Configuration.artifactGroup)
    set("PUBLISH_ARTIFACT_ID", "stream-chat-android-previewdata")
    set("PUBLISH_VERSION", rootProject.extra.get("rootVersionName"))
}

apply(from = "$rootDir/scripts/publish-module.gradle")
apply(from = "$rootDir/scripts/android.gradle")

android {
    namespace = "io.getstream.chat.android.previewdata"
    resourcePrefix = "stream_compose_previewdata"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
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
    api(project(":stream-chat-android-client"))

    detektPlugins(libs.detekt.formatting)
}
