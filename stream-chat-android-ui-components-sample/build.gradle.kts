import io.getstream.chat.android.Configuration
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.androidx.navigation)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.shot)
}

apply(from = "$rootDir/scripts/android.gradle")

android {
    namespace = "io.getstream.chat.ui.sample"
    defaultConfig {
        targetSdk = Configuration.sampleTargetSdk
        applicationId = "io.getstream.chat.ui.sample"
        versionCode = rootProject.extra.get("sampleAppVersionCode") as Int
        versionName = rootProject.extra.get("sampleAppVersionName") as String
        testInstrumentationRunner = "com.karumi.shot.ShotTestRunner"
        testApplicationId = "io.getstream.chat.ui.sample"
    }

    packaging {
        resources.excludes += setOf(
            "META-INF/*",
            "META-INF/DEPENDENCIES",
            "META-INF/LICENSE",
            "META-INF/LICENSE.txt",
            "META-INF/license.txt",
            "META-INF/NOTICE",
            "META-INF/NOTICE.txt",
            "META-INF/notice.txt",
            "META-INF/ASL2.0",
        )
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
        create("full") {
            dimension = "version"
        }
    }

    lint {
        abortOnError = false
        xmlReport = true
        checkDependencies = true
        disable += "MissingTranslation"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

androidComponents {
    beforeVariants { variantBuilder ->
        if (variantBuilder.buildType != "debug" &&
            variantBuilder.productFlavors.any { it.second.contains("full") }
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

shot {
    applicationId = "io.getstream.chat.ui.sample"
}

dependencies {
    implementation(project(":stream-chat-android-ui-components"))
    implementation(project(":stream-chat-android-markdown-transformer"))
    implementation(project(":stream-chat-android-offline"))
    implementation(files("../libraries/external/MiPush_SDK_Client_5_1_8-G_3rd.aar"))

    implementation(libs.stream.log)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.compose.material3)
    implementation(libs.stream.push.firebase)
    implementation(libs.stream.push.xiaomi)

    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.crashlytics)

    implementation(libs.coil)
    implementation(libs.coil.gif)
    implementation(libs.shimmer)

    debugImplementation(libs.androidx.fragment.testing)

    // Instrumentation tests
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.navigation.testing)

    detektPlugins(libs.detekt.formatting)
}
