import io.getstream.chat.android.Configuration
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.android.junit5)
    alias(libs.plugins.androidx.baseline.profile)
}

rootProject.extra.apply {
    set("PUBLISH_GROUP_ID", Configuration.artifactGroup)
    set("PUBLISH_ARTIFACT_ID", "stream-chat-android-ui-common")
    set("PUBLISH_VERSION", rootProject.extra.get("rootVersionName"))
}

apply(from = "$rootDir/scripts/publish-module.gradle")
apply(from = "$rootDir/scripts/android.gradle")

android {
    namespace = "io.getstream.chat.android.ui.common"
    defaultConfig {
        vectorDrawables.useSupportLibrary = true
        consumerProguardFiles("consumer-proguard-rules.pro")
    }

    resourcePrefix = "stream_"

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            unitTests.isReturnDefaultValues = true
            // // Show the result of every unit test, even if it passes.
            all {
                it.testLogging {
                    events = setOf(
                        TestLogEvent.PASSED,
                        TestLogEvent.SKIPPED,
                        TestLogEvent.FAILED,
                        TestLogEvent.STANDARD_OUT,
                        TestLogEvent.STANDARD_ERROR,
                    )
                }
            }
        }
    }

    sourceSets {
        all {
            java.srcDir("src/$name/kotlin")
        }
    }

    buildFeatures {
        compose = true
    }
}

baselineProfile {
    baselineProfileOutputDir = "."
    filter {
        include("io.getstream.chat.android.ui.common.**")
    }
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
    api(project(":stream-chat-android-state"))
    implementation(project(":stream-chat-android-ui-utils"))

    implementation(libs.stream.log)
    implementation(libs.stream.push.permissions.snackbar)
    implementation(libs.stream.result)
    implementation(libs.androidx.compose.runtime)

    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.android.material)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    implementation(libs.coil)
    implementation(libs.coil.network.okhttp)
    implementation(libs.coil.gif)
    implementation(libs.coil.video)

    testImplementation(project(":stream-chat-android-test"))
    testImplementation(testFixtures(project(":stream-chat-android-core")))
    testImplementation(libs.kotlin.reflect)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.vintage.engine)

    testImplementation(libs.threetenbp)
    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.test.parameter.injector)
    testImplementation(libs.kluent)
    testImplementation(libs.mockito)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlinx.coroutines.test)

    detektPlugins(libs.detekt.formatting)

    baselineProfile(project(":stream-chat-android-benchmark"))
}
