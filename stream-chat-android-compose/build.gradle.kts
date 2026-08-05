import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.paparazzi)
    alias(libs.plugins.stream.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.android.junit5)
    alias(libs.plugins.androidx.baseline.profile)
}

apply(from = "$rootDir/scripts/detekt-compose.gradle")

android {
    namespace = "io.getstream.chat.android.compose"
    defaultConfig {
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    resourcePrefix = "stream_"

    lint {
        disable += "MissingTranslation"
        disable += "ExtraTranslation"
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

    buildFeatures {
        compose = true
    }
}

composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
    stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("compose_compiler_config.conf"))
}

baselineProfile {
    // Do not set baselineProfileOutputDir = "." — that points the baseline-profile source dir at
    // the module's src/main root, so AGP 9's prepareReleaseArtProfile recursively parses every file
    // there (AndroidManifest.xml, res/*.webp, *.kt) as ART profile rules and fails with
    // "Illegal token '<'". The default output dir (generated/baselineProfiles) keeps it isolated.
    filter {
        include("io.getstream.chat.android.compose.**")
    }
}

// Paparazzi renders and compresses every snapshot in the forked test worker, which defaults to a
// 512 MB heap. Raise it so recordPaparazziDebug / verifyPaparazziDebug do not run out of memory.
tasks.withType<Test>().configureEach {
    maxHeapSize = "4g"
    // Forward `-Ppaparazzi.compileSdk=NN` to the forked test worker so PaparazziComposeTest can
    // render/record goldens at a chosen API level (defaults to 36). Forked workers do not inherit
    // Gradle properties, so it must be passed explicitly as a system property.
    systemProperty(
        "paparazzi.compileSdk",
        providers.gradleProperty("paparazzi.compileSdk").getOrElse("36"),
    )
}

tasks.withType<KotlinCompile> {
    compilerOptions.freeCompilerArgs.addAll(
        listOf(
            "-progressive",
            "-Xexplicit-api=strict",
            "-opt-in=io.getstream.chat.android.core.internal.InternalStreamChatApi",
            "-opt-in=io.getstream.chat.android.core.ExperimentalStreamChatApi",
            "-opt-in=kotlin.RequiresOptIn",
        ),
    )
}

dependencies {
    api(project(":stream-chat-android-ui-common"))
    implementation(project(":stream-chat-android-previewdata"))

    implementation(libs.androidx.appcompat)
    implementation(libs.stream.log)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.constraintlayout.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.google.accompanist.permissions)

    // Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.coil.network.okhttp)
    implementation(libs.coil.video)

    // Media3
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)

    // UI
    implementation(libs.reorderable)
    implementation(libs.shimmer.compose)

    // Tests
    testImplementation(project(":stream-chat-android-test"))
    testImplementation(project(":stream-chat-android-client-test"))
    testImplementation(testFixtures(project(":stream-chat-android-core")))
    testImplementation(project(":stream-chat-android-previewdata"))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.vintage.engine)

    testImplementation(libs.kluent)
    testImplementation(libs.mockito)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.turbine)
    testImplementation(libs.robolectric)

    detektPlugins(libs.detekt.formatting)

    baselineProfile(project(":stream-chat-android-benchmark"))
}
