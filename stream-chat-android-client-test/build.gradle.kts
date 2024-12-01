import io.getstream.chat.android.Dependencies
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

apply(from = "${rootDir}/scripts/android.gradle")
apply(from = "${rootDir}/scripts/detekt-test.gradle")

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll(
            listOf(
                "-progressive",
                "-Xexplicit-api=strict",
                "-opt-in=io.getstream.chat.android.core.internal.InternalStreamChatApi"
            )
        )
    }
}

android {
    namespace = "io.getstream.chat.android.client.test"
}

dependencies {
    api(project(":stream-chat-android-client"))
    implementation(project(":stream-chat-android-test"))
    implementation(testFixtures(project(":stream-chat-android-core")))

    // Kotlin
    implementation(Dependencies.kotlinReflect)
    implementation(Dependencies.coroutinesCore)
    implementation(Dependencies.coroutinesTest)

    // Google libs
    implementation(Dependencies.androidxActivityKtx)
    implementation(Dependencies.androidxAnnotations)
    implementation(Dependencies.androidxAppCompat)
    implementation(Dependencies.workRuntimeKtx)

    // Tests
    testImplementation(Dependencies.moshiKotlin)
    testImplementation(Dependencies.junitJupiterApi)
    testImplementation(Dependencies.junitJupiterParams)
    testRuntimeOnly(Dependencies.junitJupiterEngine)
    testRuntimeOnly(Dependencies.junitVintageEngine)

    testImplementation(Dependencies.androidxTest)
    testImplementation(Dependencies.androidxTestJunit)
    testImplementation(Dependencies.androidxArchCoreTest)
    testImplementation(Dependencies.androidxRecyclerview) // For performance test
    testImplementation(Dependencies.roomTesting)
    testImplementation(Dependencies.workTesting)

    testImplementation(Dependencies.robolectric)

    testImplementation(Dependencies.kluent)
    testImplementation(Dependencies.mockito)
    testImplementation(Dependencies.mockitoKotlin)
    testImplementation(Dependencies.turbine)

    // Instrumentation tests
    androidTestImplementation(Dependencies.androidxTestJunit)
    androidTestImplementation(Dependencies.espressoCore)
    androidTestImplementation(Dependencies.junit4)
    androidTestImplementation(Dependencies.kluent)
    androidTestImplementation(Dependencies.mockito)
    androidTestImplementation(Dependencies.mockitoKotlin)

    detektPlugins(Dependencies.detektFormatting)
}