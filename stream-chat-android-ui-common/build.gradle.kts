import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import io.getstream.chat.android.Configuration
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.stream.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.android.junit5)
    alias(libs.plugins.androidx.baseline.profile)
    alias(libs.plugins.maven.publish)
}

android {
    namespace = "io.getstream.chat.android.ui.common"
    defaultConfig {
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-proguard-rules.pro")
    }

    resourcePrefix = "stream_"

    lint {
        disable += "MissingTranslation"
    }

    sourceSets {
        all {
            java.srcDir("src/$name/kotlin")
        }
    }

    buildFeatures {
        compose = true
    }
}

baselineProfile {
    baselineProfileOutputDir = "."
    filter {
        include("io.getstream.chat.android.ui.common.**")
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions.freeCompilerArgs.addAll(
        listOf(
            "-progressive",
            "-Xexplicit-api=strict",
            "-opt-in=io.getstream.chat.android.core.internal.InternalStreamChatApi",
        ),
    )
}

dependencies {
    api(project(":stream-chat-android-state"))
    implementation(project(":stream-chat-android-ui-utils"))

    implementation(libs.stream.log)
    implementation(libs.stream.push.permissions.snackbar)
    implementation(libs.stream.result)
    implementation(libs.androidx.compose.runtime)

    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.android.material)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    implementation(libs.coil)
    implementation(libs.coil.network.okhttp)
    implementation(libs.coil.gif)
    implementation(libs.coil.video)

    testImplementation(project(":stream-chat-android-test"))
    testImplementation(testFixtures(project(":stream-chat-android-core")))
    testImplementation(libs.kotlin.reflect)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.vintage.engine)

    testImplementation(libs.threetenbp)
    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.androidx.test.junit)
    testImplementation(libs.test.parameter.injector)
    testImplementation(libs.kluent)
    testImplementation(libs.mockito)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.robolectric)

    detektPlugins(libs.detekt.formatting)

    baselineProfile(project(":stream-chat-android-benchmark"))
}

mavenPublishing {
    coordinates(
        groupId = Configuration.artifactGroup,
        artifactId = "stream-chat-android-ui-common",
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
