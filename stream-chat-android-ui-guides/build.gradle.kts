import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

apply(from = "$rootDir/scripts/android.gradle")
apply(from = "$rootDir/scripts/detekt-compose.gradle")

android {
    namespace = "io.getstream.chat.android.guides"
    defaultConfig {
        applicationId = "io.getstream.chat.android.guides"
        versionCode = rootProject.extra.get("sampleAppVersionCode") as Int
        versionName = rootProject.extra.get("sampleAppVersionName") as String
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    packagingOptions {
        resources.excludes.add("META-INF/*")
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    lint {
        disable += "MissingTranslation"
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
    implementation(project(":stream-chat-android-state"))
    implementation(project(":stream-chat-android-offline"))
    implementation(project(":stream-chat-android-ui-components"))
    implementation(project(":stream-chat-android-compose"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.android.material)

    // Compose
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    detektPlugins(libs.detekt.formatting)
}
