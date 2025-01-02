import io.getstream.chat.android.Configuration
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.junit5)
}

rootProject.extra.apply {
    set("PUBLISH_GROUP_ID", Configuration.artifactGroup)
    set("PUBLISH_ARTIFACT_ID", "stream-chat-android-markdown-transformer")
    set("PUBLISH_VERSION", rootProject.extra.get("rootVersionName"))
}

apply(from = "$rootDir/scripts/publish-module.gradle")
apply(from = "$rootDir/scripts/android.gradle")

android {
    namespace = "io.getstream.chat.android.ui.markdown"
    sourceSets {
        all {
            java.srcDir("src/$name/kotlin")
        }
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

dependencies {
    implementation(project(":stream-chat-android-ui-components"))

    implementation(libs.androidx.core.ktx)

    // Markdown
    implementation(libs.markwon.core)
    implementation(libs.markwon.linkify)
    implementation(libs.markwon.ext.strikethrough)
    implementation(libs.markwon.image)

    // Tests
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)

    detektPlugins(libs.detekt.formatting)
}
