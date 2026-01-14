import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import io.getstream.chat.android.Configuration
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.stream.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.android.junit5)
    alias(libs.plugins.androidx.baseline.profile)
    alias(libs.plugins.paparazzi)
    alias(libs.plugins.maven.publish)
}

apply(from = "$rootDir/scripts/detekt-compose.gradle")

android {
    namespace = "io.getstream.chat.android.compose"
    defaultConfig {
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    resourcePrefix = "stream_compose_"

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
    enableStrongSkippingMode = true
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
    stabilityConfigurationFile = rootProject.layout.projectDirectory.file("compose_compiler_config.conf")
}

baselineProfile {
    baselineProfileOutputDir = "."
    filter {
        include("io.getstream.chat.android.compose.**")
    }
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

mavenPublishing {
    coordinates(
        groupId = Configuration.artifactGroup,
        artifactId = "stream-chat-android-compose",
        version = rootProject.version.toString(),
    )
    configure(
        AndroidSingleVariantLibrary(
            variant = "release",
            sourcesJar = true,
            publishJavadocJar = true,
        ),
    )
}
