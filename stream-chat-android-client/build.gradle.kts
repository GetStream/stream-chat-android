import io.getstream.chat.android.Dependencies
import io.getstream.chat.android.Configuration
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("com.google.devtools.ksp")
    id("de.mannodermaus.android-junit5")
    id("androidx.baselineprofile")
}

val PUBLISH_GROUP_ID by extra(Configuration.artifactGroup)
val PUBLISH_ARTIFACT_ID by extra("stream-chat-android-client")
val PUBLISH_VERSION: String by extra(properties["rootVersionName"] as String)

apply(from = "${rootDir}/scripts/publish-module.gradle")
apply(from = "${rootDir}/scripts/android.gradle")

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
                "proguard-rules.pro"
            )
            consumerProguardFiles("consumer-proguard-rules.pro")
        }
        getByName("debug") {
            enableUnitTestCoverage = false
            enableAndroidTestCoverage = false
            consumerProguardFiles("consumer-proguard-rules.pro")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    lint {
        xmlReport = false
    }

    testOptions.unitTests {
        isReturnDefaultValues = true
    }

    resourcePrefix = "stream_"
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
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=io.getstream.chat.android.core.internal.InternalStreamChatApi"
            )
        )
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    api(project(":stream-chat-android-core"))

    implementation(Dependencies.kotlinReflect)
    implementation(Dependencies.androidxAppCompat)
    implementation(Dependencies.androidxCoreKtx)
    implementation(Dependencies.androidxLifecycleProcess)
    implementation(Dependencies.androidxLifecycleLiveDataKtx)
    implementation(Dependencies.workRuntimeKtx)
    implementation(Dependencies.constraintLayout)
    implementation(Dependencies.coroutinesCore)
    implementation(Dependencies.coroutinesAndroid)
    implementation(Dependencies.retrofit)
    implementation(Dependencies.streamLog)
    debugImplementation(Dependencies.streamLogAndroidFile)
    implementation(Dependencies.streamPushDelegate)
    api(Dependencies.streamPushPermissions)
    implementation(Dependencies.ituDate)
    implementation(Dependencies.moshi)
    implementation(Dependencies.retrofitMoshiConverter)
    ksp(Dependencies.moshiCodegen)
    implementation(Dependencies.okhttpLoggingInterceptor)
    implementation(Dependencies.ok2curl)
    debugImplementation(Dependencies.flipper)
    debugImplementation(Dependencies.flipperNetwork)
    debugImplementation(Dependencies.flipperLoader)

    // Tests
    testImplementation(project(":stream-chat-android-test"))
    testImplementation(project(":stream-chat-android-client-test"))
    testImplementation(testFixtures(project(":stream-chat-android-core")))
    testImplementation(Dependencies.streamResult)
    testImplementation(Dependencies.androidxTestJunit)
    testImplementation(Dependencies.androidxLifecycleTesting)
    testImplementation(Dependencies.junitJupiterApi)
    testImplementation(Dependencies.junitJupiterParams)
    testRuntimeOnly(Dependencies.junitJupiterEngine)
    testRuntimeOnly(Dependencies.junitVintageEngine)
    testImplementation(Dependencies.json)
    testImplementation(Dependencies.kluent)
    testImplementation(Dependencies.mockito)
    testImplementation(Dependencies.mockitoKotlin)
    testImplementation(Dependencies.okhttpMockWebserver)
    testImplementation(Dependencies.robolectric)

    // Instrumentation tests
    androidTestImplementation(Dependencies.junit4)
    androidTestImplementation(Dependencies.espressoCore)
    androidTestImplementation(Dependencies.androidxTestJunit)

    detektPlugins(Dependencies.detektFormatting)

    baselineProfile(project(":stream-chat-android-benchmark"))
}

/*
// Uncomment if the Dokka page per module is required
tasks.withType<org.jetbrains.dokka.gradle.DokkaTaskPartial> {
    dokkaSourceSets {
        named("main") {
            moduleName.set("LLC")
            includes.from("DokkaModule.md")
        }
    }
}
