plugins {
    alias(libs.plugins.stream.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

apply(from = "${rootDir}/scripts/detekt-disabled.gradle")

android {
    namespace = "io.getstream.chat.docs"
    defaultConfig {
        applicationId = "io.getstream.chat.docs"
    }

    signingConfigs {
        create("release") {
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            storeFile = rootProject.file(".sign/debug.keystore")
            storePassword = "android"
        }

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
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    lint {
        abortOnError = false
        xmlReport = true
        checkDependencies = true
    }

    packaging {
        resources.excludes += setOf(
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

    buildFeatures {
        viewBinding = true
        compose = true
    }
}

repositories {

}

dependencies {
    implementation(project(":stream-chat-android-offline"))
    implementation(project(":stream-chat-android-state"))
    implementation(project(":stream-chat-android-ui-components"))
    implementation(project(":stream-chat-android-compose"))
    implementation(project(":stream-chat-android-markdown-transformer"))


    // Compose
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.foundation)

    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.google.accompanist.permissions)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)


    implementation(libs.stream.push.firebase)
    implementation(libs.stream.push.huawei)
    implementation(libs.stream.push.xiaomi)

    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.android.material)
    implementation(libs.shimmer)
    implementation(libs.firebase.messaging)
    implementation(libs.huawei.push)
    compileOnly(files("../libraries/external/MiPush_SDK_Client_5_1_8-G_3rd.aar"))
    implementation(libs.coil)
    implementation(libs.coil.compose)
}
