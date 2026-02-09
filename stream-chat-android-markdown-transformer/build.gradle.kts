import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import io.getstream.chat.android.Configuration
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.stream.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.junit5)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.dokka)
}

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

mavenPublishing {
    coordinates(
        groupId = Configuration.artifactGroup,
        artifactId = "stream-chat-android-markdown-transformer",
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
