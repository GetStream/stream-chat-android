import io.getstream.chat.android.Configuration
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-library")
    id("java-test-fixtures")
    id("kotlin")
}

rootProject.extra.apply {
    set("PUBLISH_GROUP_ID", Configuration.artifactGroup)
    set("PUBLISH_ARTIFACT_ID", "stream-chat-android-core")
    set("PUBLISH_VERSION", rootProject.extra.get("rootVersionName"))
}

apply(from = "$rootDir/scripts/publish-module.gradle")
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withSourcesJar()
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
        jvmTarget.set(JvmTarget.JVM_11)
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
