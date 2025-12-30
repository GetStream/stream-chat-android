import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.stream.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.androidx.baseline.profile)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.google.services)
}

apply(from = "$rootDir/scripts/detekt-compose.gradle")

android {
    namespace = "io.getstream.chat.android.compose.sample"
    defaultConfig {
        targetSdk = libs.versions.sampleTargetSdk.get().toInt()
        applicationId = "io.getstream.chat.android.compose.sample"
        versionCode = rootProject.extra.get("sampleAppVersionCode") as Int
        versionName = rootProject.extra.get("sampleAppVersionName") as String
        testInstrumentationRunner = "io.qameta.allure.android.runners.AllureAndroidJUnitRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }

    val signFile = rootProject.file(".sign/keystore.properties")
    if (signFile.exists()) {
        val properties = Properties()
        properties.load(FileInputStream(signFile))

        signingConfigs {
            create("release") {
                keyAlias = properties["keyAlias"] as? String
                keyPassword = properties["keyPassword"] as? String
                storeFile = rootProject.file(properties["keystore"] as String)
                storePassword = properties["storePassword"] as? String
            }
        }
    } else {
        signingConfigs {
            create("release") {
                keyAlias = "androiddebugkey"
                keyPassword = "android"
                storeFile = rootProject.file(".sign/debug.keystore")
                storePassword = "android"
            }
        }
    }

    signingConfigs {
        getByName("debug") {
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            storeFile = rootProject.file(".sign/debug.keystore")
            storePassword = "android"
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
            matchingFallbacks += listOf("release")
            proguardFiles("benchmark-rules.pro")
            buildConfigField("Boolean", "BENCHMARK", "true")
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("Boolean", "BENCHMARK", "false")
        }
    }

    flavorDimensions += "version"
    productFlavors {
        create("demo") {
            dimension = "version"
        }
        create("e2e") {
            dimension = "version"
            applicationIdSuffix = ".e2etest"
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        disable += "MissingTranslation"
    }
}

androidComponents {
    beforeVariants { variantBuilder ->
        if (variantBuilder.buildType != "debug" &&
            variantBuilder.productFlavors.any { it.second.contains("e2e") }
        ) {
            variantBuilder.enable = false
        }
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions.freeCompilerArgs.addAll(
        listOf(
            "-opt-in=kotlin.RequiresOptIn",
        ),
    )
}

dependencies {
    implementation(project(":stream-chat-android-compose"))
    implementation(project(":stream-chat-android-ui-utils"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.android.material)
    implementation(libs.stream.log)

    // Compose
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.google.accompanist.permissions)
    implementation(libs.coil.compose)

    implementation(libs.play.services.location)

    // Firebase - both flavors need it for compilation
    // demo: real Firebase with actual push notifications
    // e2e: fake google-services.json, Firebase present but not actively used
    implementation(libs.stream.push.firebase)
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.crashlytics)

    // Instrumentation tests
    "e2eImplementation"(libs.okhttp)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.uiautomator)
    androidTestImplementation(libs.androidx.test.junit.ktx)
    androidTestImplementation(libs.androidx.test.monitor)
    androidTestUtil(libs.androidx.test.orchestrator)
    androidTestImplementation(libs.allure.kotlin.model)
    androidTestImplementation(libs.allure.kotlin.junit4)
    androidTestImplementation(libs.allure.kotlin.commons)
    androidTestImplementation(libs.allure.kotlin.android)
    androidTestImplementation(project(":stream-chat-android-e2e-test"))

    detektPlugins(libs.detekt.formatting)

    baselineProfile(project(":stream-chat-android-benchmark"))
}
