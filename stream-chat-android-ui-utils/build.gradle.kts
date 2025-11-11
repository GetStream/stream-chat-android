import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import io.getstream.chat.android.Configuration
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.stream.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.junit5)
    alias(libs.plugins.maven.publish)
}

android {
    namespace = "io.getstream.chat.android.ui.utils"
    defaultConfig {
        vectorDrawables.useSupportLibrary = true
    }

    resourcePrefix = "stream_"

    sourceSets {
        all {
            java.srcDir("src/$name/kotlin")
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll(
            listOf(
                "-progressive",
                "-Xexplicit-api=strict",
                "-opt-in=io.getstream.chat.android.core.internal.InternalStreamChatApi",
            ),
        )
    }
}

dependencies {
    implementation(project(":stream-chat-android-client"))
    implementation(project(":stream-chat-android-state"))

    implementation(libs.androidx.annotation)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(project(":stream-chat-android-test"))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.vintage.engine)

    testImplementation(testFixtures(project(":stream-chat-android-core")))

    testImplementation(libs.kluent)
    testImplementation(libs.mockito)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlinx.coroutines.test)

    detektPlugins(libs.detekt.formatting)
}

mavenPublishing {
    coordinates(
        groupId = Configuration.artifactGroup,
        artifactId = "stream-chat-android-ui-utils",
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
