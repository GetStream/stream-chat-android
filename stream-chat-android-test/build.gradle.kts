import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.stream.android.library)
    alias(libs.plugins.kotlin.android)
}
apply(from = "$rootDir/scripts/detekt-test.gradle")

android {
    namespace = "io.getstream.chat.android.test"
}

tasks.withType<KotlinCompile> {
    compilerOptions.freeCompilerArgs.addAll(
        listOf(
            "-progressive",
            "-Xexplicit-api=strict",
            "-opt-in=io.getstream.chat.android.core.internal.InternalStreamChatApi",
        ),
    )
}

dependencies {
    api(project(":stream-chat-android-core"))

    implementation(libs.androidx.lifecycle.livedata.ktx)
    api(libs.kotlinx.coroutines.test)
    implementation(libs.androidx.core.testing)
    implementation(libs.junit.jupiter.api)
    implementation(libs.junit)
    implementation(libs.stream.log)

    detektPlugins(libs.detekt.formatting)
}
