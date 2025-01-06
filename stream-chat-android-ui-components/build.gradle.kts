import io.getstream.chat.android.Configuration
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.android.junit5)
    alias(libs.plugins.androidx.baseline.profile)
}

rootProject.extra.apply {
    set("PUBLISH_GROUP_ID", Configuration.artifactGroup)
    set("PUBLISH_ARTIFACT_ID", "stream-chat-android-ui-components")
    set("PUBLISH_VERSION", rootProject.extra.get("rootVersionName"))
}

apply(from = "$rootDir/scripts/publish-module.gradle")
apply(from = "$rootDir/scripts/android.gradle")

android {
    namespace = "io.getstream.chat.android.ui"
    defaultConfig {
        vectorDrawables.useSupportLibrary = true
    }

    resourcePrefix = "stream_ui_"

    buildFeatures {
        viewBinding = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            unitTests.isReturnDefaultValues = true
            // Show the result of every unit test, even if it passes.
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
}

baselineProfile {
    baselineProfileOutputDir = "."
    filter {
        include("io.getstream.chat.android.ui.**")
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions.freeCompilerArgs.addAll(
        listOf(
            "-progressive",
            "-Xexplicit-api=strict",
            "-opt-in=kotlin.contracts.ExperimentalContracts",
            "-opt-in=io.getstream.chat.android.core.internal.InternalStreamChatApi",
            "-opt-in=io.getstream.chat.android.core.ExperimentalStreamChatApi",
        ),
    )
}

dependencies {
    api(project(":stream-chat-android-ui-common"))
    implementation(project(":stream-chat-android-ui-utils"))

    implementation(libs.stream.log)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.legacy.support)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.android.material)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.core.ktx)
    implementation(libs.stream.photoview.dialog)
    implementation(libs.coil)
    implementation(libs.coil.gif)
    implementation(libs.coil.video)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.keyboardvisibilityevent)
    implementation(libs.permissionx)

    // Tests
    testImplementation(project(":stream-chat-android-test"))
    testImplementation(testFixtures(project(":stream-chat-android-core")))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.vintage.engine)
    testImplementation(libs.kluent)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.androidx.core.testing)

    detektPlugins(libs.detekt.formatting)

    baselineProfile(project(":stream-chat-android-benchmark"))
}
