import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply(from = "$rootDir/scripts/android.gradle")
apply(from = "$rootDir/scripts/detekt-test.gradle")

tasks.withType<KotlinCompile> {
    compilerOptions.freeCompilerArgs.addAll(
        listOf(
            "-progressive",
            "-Xexplicit-api=strict",
            "-opt-in=io.getstream.chat.android.core.internal.InternalStreamChatApi",
        ),
    )
}

android {
    namespace = "io.getstream.chat.android.e2e.test"
}

dependencies {
    implementation(libs.okhttp)
    implementation(libs.androidx.test.core)
    implementation(libs.androidx.test.uiautomator)
    implementation(libs.androidx.test.monitor)
    implementation(libs.androidx.test.junit.ktx)

    detektPlugins(libs.detekt.formatting)
}
