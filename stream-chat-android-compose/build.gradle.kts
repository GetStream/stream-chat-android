import io.getstream.chat.android.Configuration
import io.getstream.chat.android.Dependencies
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("kotlin-parcelize")
    id("de.mannodermaus.android-junit5")
    id("app.cash.paparazzi")
    id("androidx.baselineprofile")
}

val PUBLISH_GROUP_ID by extra(Configuration.artifactGroup)
val PUBLISH_ARTIFACT_ID by extra("stream-chat-android-compose")
val PUBLISH_VERSION by extra(rootProject.extra["rootVersionName"] as String)

apply(from = "${rootDir}/scripts/publish-module.gradle")
apply(from = "${rootDir}/scripts/android.gradle")
apply(from = "${rootDir}/scripts/detekt-compose.gradle")

android {
    namespace = "io.getstream.chat.android.compose"
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    resourcePrefix = "stream_compose_"

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
    }
}

composeCompiler {
    enableStrongSkippingMode = true
    reportsDestination.set(layout.buildDirectory.dir("compose_compiler"))
    stabilityConfigurationFile.set(rootProject.layout.projectDirectory.file("compose_compiler_config.conf"))
}

baselineProfile {
    baselineProfileOutputDir = "."
    filter {
        include("io.getstream.chat.android.compose.**")
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll(
            listOf(
                "-progressive",
                "-Xexplicit-api=strict",
                "-opt-in=io.getstream.chat.android.core.internal.InternalStreamChatApi",
                "-opt-in=io.getstream.chat.android.core.ExperimentalStreamChatApi",
                "-opt-in=kotlin.RequiresOptIn"
            )
        )
    }
}

dependencies {
    api(project(":stream-chat-android-ui-common"))
    implementation(project(":stream-chat-android-previewdata"))
    implementation(project(":stream-chat-android-ui-utils"))

    implementation(Dependencies.androidxAppCompat)
    implementation(Dependencies.streamLog)

    // Compose
    implementation(Dependencies.composeUi)
    implementation(Dependencies.composeAndroidLifecycle)
    implementation(Dependencies.composeUiTooling)
    implementation(Dependencies.composeFoundation)
    implementation(Dependencies.composeConstraintLayout)
    implementation(Dependencies.composeMaterial)
    implementation(Dependencies.composeMaterial3)

    implementation(Dependencies.composeActivity)
    implementation(Dependencies.composeViewModel)
    implementation(Dependencies.composeAccompanistPermissions)
    implementation(Dependencies.composeAccompanistPager)
    implementation(Dependencies.composeAccompanistSystemUiController)

    // Coil
    implementation(Dependencies.composeCoil)
    implementation(Dependencies.composeLandscapistCoil)
    implementation(Dependencies.composeLandscapistPlaceholder)
    implementation(Dependencies.composeLandscapistAnimation)
    implementation(Dependencies.coilGif)
    implementation(Dependencies.coilVideo)

    // UI
    implementation(Dependencies.reorderable)

    // Tests
    testImplementation(project(":stream-chat-android-test"))
    testImplementation(testFixtures(project(":stream-chat-android-core")))
    testImplementation(project(":stream-chat-android-previewdata"))
    testImplementation(Dependencies.junitJupiterApi)
    testImplementation(Dependencies.junitJupiterParams)
    testRuntimeOnly(Dependencies.junitJupiterEngine)
    testRuntimeOnly(Dependencies.junitVintageEngine)

    testImplementation(Dependencies.kluent)
    testImplementation(Dependencies.mockito)
    testImplementation(Dependencies.mockitoKotlin)

    detektPlugins(Dependencies.detektFormatting)

    baselineProfile(project(":stream-chat-android-benchmark"))
}