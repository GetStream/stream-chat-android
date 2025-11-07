import io.getstream.chat.android.Configuration
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.stream.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.junit5)
    alias(libs.plugins.androidx.baseline.profile)
}

rootProject.extra.apply {
    set("PUBLISH_GROUP_ID", Configuration.artifactGroup)
    set("PUBLISH_ARTIFACT_ID", "stream-chat-android-client")
    set("PUBLISH_VERSION", rootProject.extra.get("rootVersionName"))
}

apply(from = "$rootDir/scripts/publish-module.gradle")

android {
    namespace = "io.getstream.chat.android.client"
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "STREAM_CHAT_VERSION", "\"$version\"")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            consumerProguardFiles("consumer-proguard-rules.pro")
        }
        getByName("debug") {
            consumerProguardFiles("consumer-proguard-rules.pro")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    resourcePrefix = "stream_"

    lint {
        xmlReport = false
    }

    testOptions.unitTests {
        isReturnDefaultValues = true
    }
}

baselineProfile {
    baselineProfileOutputDir = "."
    filter {
        include("io.getstream.chat.android.client.**")
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll(
            listOf(
                "-progressive",
                "-Xexplicit-api=strict",
                "-Xjvm-default=all",
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=io.getstream.chat.android.core.internal.InternalStreamChatApi",
            ),
        )
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    api(project(":stream-chat-android-core"))

    implementation(libs.kotlin.reflect)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.work)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.retrofit)
    implementation(libs.stream.log)
    implementation(libs.stream.push.delegate)
    api(libs.stream.push.permissions)
    implementation(libs.itu.date.version)
    implementation(libs.moshi)
    implementation(libs.retrofit.converter.moshi)
    ksp(libs.moshi.codegen)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.ok2curl)

    // Unused dependencies: The following dependencies (appcompat, constraintlayout, livedata-ktx) are not used in the
    // `stream-chat-android-client` module. They are still declared here to prevent potential breaking changes for
    // integrations that might be relying on them transitively. Consider removing them in future major releases.
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Tests
    testImplementation(project(":stream-chat-android-test"))
    testImplementation(project(":stream-chat-android-client-test"))
    testImplementation(testFixtures(project(":stream-chat-android-core")))
    testImplementation(libs.stream.result)
    testImplementation(libs.androidx.test.junit)
    testImplementation(libs.androidx.lifecycle.runtime.testing)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.vintage.engine)

    testImplementation(libs.json)
    testImplementation(libs.kluent)
    testImplementation(libs.mockito)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotest.assertions.json)

    // Instrumentation tests
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.junit)

    detektPlugins(libs.detekt.formatting)

    baselineProfile(project(":stream-chat-android-benchmark"))
}

/* Uncomment if the Dokka page per module is required
tasks.withType(dokkaHtmlPartial.getClass()) {
    dokkaSourceSets {
        named("main") {
            moduleName.set("LLC")
            includes.from("DokkaModule.md")
        }
    }
}
*/
