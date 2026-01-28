import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import io.getstream.chat.android.Configuration
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.paparazzi)
    alias(libs.plugins.stream.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.android.junit5)
    alias(libs.plugins.androidx.baseline.profile)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.dokka)
}

android {
    namespace = "io.getstream.chat.android.ui"
    defaultConfig {
        vectorDrawables.useSupportLibrary = true
    }

    resourcePrefix = "stream_ui_"

    lint {
        disable += "MissingTranslation"
        disable += "ExtraTranslation"
        baseline = file("lint-baseline.xml")
    }

    buildFeatures {
        viewBinding = true
    }

    sourceSets {
        all {
            java.srcDir("src/$name/kotlin")
        }
    }
}

baselineProfile {
    baselineProfileOutputDir = "."
    filter {
        include("io.getstream.chat.android.ui.**")
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions.freeCompilerArgs.addAll(
        listOf(
            "-progressive",
            "-Xexplicit-api=strict",
            "-opt-in=kotlin.contracts.ExperimentalContracts",
            "-opt-in=io.getstream.chat.android.core.internal.InternalStreamChatApi",
            "-opt-in=io.getstream.chat.android.core.ExperimentalStreamChatApi",
        ),
    )
}

dependencies {
    api(project(":stream-chat-android-ui-common"))
    implementation(project(":stream-chat-android-ui-utils"))

    implementation(libs.stream.log)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.legacy.support)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.android.material)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.stream.photoview.dialog)
    implementation(libs.coil)
    implementation(libs.coil.gif)
    implementation(libs.coil.video)
    implementation(libs.okhttp)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.keyboardvisibilityevent)
    implementation(libs.permissionx)

    // Tests
    testImplementation(project(":stream-chat-android-test"))
    testImplementation(project(":stream-chat-android-previewdata"))
    testImplementation(project(":stream-chat-android-client-test"))
    testImplementation(testFixtures(project(":stream-chat-android-core")))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.vintage.engine)
    testImplementation(libs.kluent)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.androidx.core.testing)

    detektPlugins(libs.detekt.formatting)

    baselineProfile(project(":stream-chat-android-benchmark"))
}

mavenPublishing {
    coordinates(
        groupId = Configuration.artifactGroup,
        artifactId = "stream-chat-android-ui-components",
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
