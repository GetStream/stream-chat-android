import io.getstream.chat.android.Dependencies
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("androidx.baselineprofile")
}

apply(from = "${rootDir}/scripts/android.gradle")
apply(from = "${rootDir}/scripts/detekt-compose.gradle")

android {
    namespace = "io.getstream.chat.android.compose.sample"
    defaultConfig {
        targetSdk = io.getstream.chat.android.Configuration.sampleTargetSdk
        applicationId = "io.getstream.chat.android.compose.sample"
        versionCode = 1 // sampleAppVersionCode
        versionName = "1.0.0" // sampleAppVersionName
        testInstrumentationRunner = "io.qameta.allure.android.runners.AllureAndroidJUnitRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
    }

    testOptions.execution = "ANDROIDX_TEST_ORCHESTRATOR"

    val signFile = rootProject.file(".sign/keystore.properties")
    signingConfigs {
        getByName("debug") {
            storeFile = rootProject.file(".sign/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }

        create("release") {
            if (signFile.exists()) {
                val properties = Properties().apply {
                    load(FileInputStream(signFile))
                }
                storeFile = rootProject.file(properties["keystore"] as String)
                storePassword = properties["storePassword"] as String
                keyAlias = properties["keyAlias"] as String
                keyPassword = properties["keyPassword"] as String
            } else {
                storeFile = rootProject.file(".sign/debug.keystore")
                storePassword = "android"
                keyAlias = "androiddebugkey"
                keyPassword = "android"
            }
        }
    }

    buildTypes {
        getByName("debug") {
            versionNameSuffix = "-DEBUG"
            applicationIdSuffix = ".debug"
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("debug")
            buildConfigField("Boolean", "BENCHMARK", "false")
        }
        create("benchmark") {
            isDebuggable = false
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks.add("release")
            proguardFiles("benchmark-rules.pro")
            buildConfigField("Boolean", "BENCHMARK", "true")
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("Boolean", "BENCHMARK", "false")
        }
    }

    flavorDimensions.add("version")
    productFlavors {
        create("demo") {
            dimension = "version"
        }
        create("e2e") {
            dimension = "version"
            applicationIdSuffix = ".e2etest"
        }
    }

    androidComponents {
        beforeVariants { variantBuilder ->
            if (variantBuilder.buildType != "debug" && variantBuilder.productFlavors.any { it.second == "e2e" }) {
                variantBuilder.enable = false
            }
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        disable.add("MissingTranslation")
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
    }
}

dependencies {
    implementation(project(":stream-chat-android-compose"))
    implementation(project(":stream-chat-android-offline"))

    implementation(Dependencies.androidxCoreKtx)
    implementation(Dependencies.androidxAppCompat)
    implementation(Dependencies.materialComponents)
    implementation(Dependencies.streamPushFirebase)
    implementation(Dependencies.streamLog)

    // Compose
    implementation(Dependencies.composeUi)
    implementation(Dependencies.composeUiTooling)
    implementation(Dependencies.composeFoundation)
    implementation(Dependencies.composeMaterial)
    implementation(Dependencies.composeMaterialIcons)

    implementation(Dependencies.composeActivity)
    implementation(Dependencies.composeAndroidLifecycle)
    implementation(Dependencies.composeViewModel)
    implementation(Dependencies.composeAccompanistPermissions)
    implementation(Dependencies.composeAccompanistPager)

    // Coil
    implementation(Dependencies.composeLandscapistCoil)

    // Firebase
    implementation(Dependencies.firebaseAnalytics)
    implementation(Dependencies.firebaseCrashlytics)

    // Instrumentation tests
    androidTestImplementation(Dependencies.androidxTestRunner)
    androidTestImplementation(Dependencies.androidxUiAutomator)
    androidTestImplementation(Dependencies.androidxTestJunitKtx)
    androidTestImplementation(Dependencies.androidxTestMonitor)
    androidTestUtil(Dependencies.androidxTestOrchestrator)
    androidTestImplementation(Dependencies.allureKotlinModel)
    androidTestImplementation(Dependencies.allureKotlinJunit)
    androidTestImplementation(Dependencies.allureKotlinCommons)
    androidTestImplementation(Dependencies.allureKotlinAndroid)
    androidTestImplementation(project(":stream-chat-android-e2e-test"))

    detektPlugins(Dependencies.detektFormatting)

    baselineProfile(project(":stream-chat-android-benchmark"))
}