import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import io.getstream.chat.android.Configuration
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.stream.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.dokka)
}

apply(from = "$rootDir/scripts/detekt-compose.gradle")

android {
    namespace = "io.getstream.chat.android.ai.assistant"
    resourcePrefix = "stream_ai_assistant"

    buildFeatures {
        compose = true
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions.freeCompilerArgs.addAll(
        listOf(
            "-Xexplicit-api=strict",
            "-opt-in=io.getstream.chat.android.core.internal.InternalStreamChatApi",
        ),
    )
}

composeCompiler {
    enableStrongSkippingMode = true
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
}

dependencies {
    api(project(":stream-chat-android-compose"))

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.shimmer.compose)
    implementation(libs.compose.markdown)

    detektPlugins(libs.detekt.formatting)
}

mavenPublishing {
    coordinates(
        groupId = Configuration.artifactGroup,
        artifactId = "stream-chat-android-ai-assistant",
        version = rootProject.version.toString(),
    )
    configure(
        AndroidSingleVariantLibrary(
            variant = "release",
            sourcesJar = true,
            publishJavadocJar = true,
        ),
    )
}
