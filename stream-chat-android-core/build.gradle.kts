import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("kotlin")
    alias(libs.plugins.stream.java.library)
    id("java-test-fixtures")
}

tasks.withType<Test>() {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll(
            listOf(
                "-progressive",
                "-Xconsistent-data-class-copy-visibility",
                "-Xexplicit-api=strict",
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=io.getstream.chat.android.core.internal.InternalStreamChatApi",
            ),
        )
    }
}

dependencies {
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.annotation)
    compileOnly(libs.skydoves.compose.stable.marker)

    api(libs.stream.result)
    api(libs.stream.result.call)
    implementation(libs.stream.log)

    detektPlugins(libs.detekt.formatting)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.kluent)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockito)
    testImplementation(libs.mockito.kotlin)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.vintage.engine)
}
