import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.shot)
}

apply(from = "$rootDir/scripts/android.gradle")

android {
    namespace = "io.getstream.chat.android.uitests"
    defaultConfig {
        applicationId = "io.getstream.chat.android.uitests"
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.karumi.shot.ShotTestRunner"
        testApplicationId = "io.getstream.chat.android.uitests.test"
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

    lintOptions {
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

shot {
    applicationId = "io.getstream.chat.android.uitests"
    tolerance = 1.0
}

dependencies {
    implementation(project(":stream-chat-android-offline"))
    implementation(project(":stream-chat-android-ui-components"))
    implementation(project(":stream-chat-android-compose"))

    androidTestImplementation(project(":stream-chat-android-test"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.android.material)

    // Compose
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.google.accompanist.permissions)
    implementation(libs.skydoves.landscapist.coil)

    // Coil
    androidTestImplementation(libs.coil)
    androidTestImplementation(libs.coil.gif)
    androidTestImplementation(libs.coil.video)

    // Instrumentation tests
    debugImplementation(libs.androidx.fragment.testing) {
        exclude(group = "androidx.test", module = "monitor")
    }
    androidTestImplementation(libs.androidx.test.espresso.contrib) {
        exclude(group = "org.checkerframework", module = "checker")
    }
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.core.testing)
    androidTestImplementation(libs.androidx.test.espresso.idling.resource)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.navigation.testing)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(libs.test.parameter.injector)
    androidTestImplementation(libs.okhttp.mockwebserver)
    androidTestImplementation(libs.threetenbp)

    detektPlugins(libs.detekt.formatting)
}
