plugins {
    alias(libs.plugins.stream.android.library)
    alias(libs.plugins.kotlin.android)
}

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
