import com.android.build.api.dsl.ApplicationExtension
import com.android.build.gradle.LibraryExtension
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import io.getstream.chat.android.Dependencies
import io.getstream.chat.android.command.changelog.task.ChangelogReleaseSectionTask
import io.getstream.chat.android.command.release.task.ReleaseTask

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.androidx.baseline.profile) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.android.junit5) apply false
    alias(libs.plugins.paparazzi) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.stream.project)
    alias(libs.plugins.stream.android.library) apply false
    alias(libs.plugins.stream.android.application) apply false
    alias(libs.plugins.stream.java.library) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.shot) apply false
    alias(libs.plugins.androidx.navigation) apply false
    id("io.getstream.chat.ReleasePlugin")
    id("io.getstream.chat.ChangelogReleaseSectionPlugin")
    alias(libs.plugins.gitversioner)
    alias(libs.plugins.gradle.versions)
    alias(libs.plugins.binary.compatibility.validator)
}

// TODO [G.]
// Configuration.kt is still referenced:
// - publish-new-version.yml
// - buildSrc/src/main/kotlin/io/getstream/chat/android/command/version/task/Constants.kt

streamProject {
    spotless {
        ignoredModules = setOf("stream-chat-android-docs")
    }

    coverage {
        includedModules = setOf(
            "stream-chat-android-client",
            "stream-chat-android-compose",
            "stream-chat-android-core",
            "stream-chat-android-markdown-transformer",
            "stream-chat-android-offline",
            "stream-chat-android-state",
            "stream-chat-android-ui-common",
            "stream-chat-android-ui-utils",
        )
    }

    publishing {
        description = "Stream Chat official Android SDK"
    }
}

afterEvaluate {
    tasks.named("testCoverage") {
        // Run Paparazzi tests of the UI Components module but do not include its coverage in the report
        // until we can update Paparazzi and Kover libraries alongside Kotlin and Compose.
        // See https://linear.app/stream/issue/AND-819/update-paparazzi-and-kover-libraries
        dependsOn("stream-chat-android-ui-components:verifyPaparazziDebug")
    }
}

buildscript {
    dependencies {
        // TODO: Remove this workaround after AGP 8.9.0 is released
        // Workaround for integrate sonarqube plugin with AGP
        // It looks like will be fixed after AGP 8.9.0-alpha04 is released
        // https://issuetracker.google.com/issues/380600747?pli=1
        classpath("org.bouncycastle:bcutil-jdk18on:1.79")
    }
}

apply(from = "${rootDir}/scripts/sample-app-versioner.gradle")

subprojects {
    // Configure Android projects with common SDK versions as soon as either plugin is applied
    pluginManager.withPlugin("com.android.library") {
        extensions.configure<LibraryExtension> {
            defaultConfig {
                compileSdk = libs.versions.compileSdk.get().toInt()
                minSdk = libs.versions.minSdk.get().toInt()
                lint.targetSdk = libs.versions.targetSdk.get().toInt()
                testOptions.targetSdk = libs.versions.targetSdk.get().toInt()
            }
        }
    }
    pluginManager.withPlugin("com.android.application") {
        extensions.configure<ApplicationExtension> {
            defaultConfig {
                compileSdk = libs.versions.compileSdk.get().toInt()
                minSdk = libs.versions.minSdk.get().toInt()
                targetSdk = libs.versions.targetSdk.get().toInt()
            }
        }
    }

    apply(plugin = "io.gitlab.arturbosch.detekt")
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        Dependencies.isStable(currentVersion) && Dependencies.isNonStable(candidate.version)
    }
}

tasks.withType<ReleaseTask> {
    config.changelogPath = "CHANGELOG.md"
}

tasks.withType<ChangelogReleaseSectionTask> {
    config.changelogPath = "CHANGELOG.md"
}

tasks.withType<Delete> {
    delete(rootProject.layout.buildDirectory)
}

apiValidation {
    ignoredPackages.add("com/getstream/sdk/chat/databinding")
    ignoredPackages.add("io/getstream/chat/android/ui/databinding")

    ignoredProjects += listOf(
        "stream-chat-android-client-test",
        "stream-chat-android-compose-sample",
        "stream-chat-android-docs",
        "stream-chat-android-e2e-test",
        "stream-chat-android-previewdata",
        "stream-chat-android-test",
        "stream-chat-android-ui-components-sample",
        "stream-chat-android-ui-guides",
        "stream-chat-android-ui-uitests",
        "stream-chat-android-metrics",
    )

    nonPublicMarkers += listOf(
        "io.getstream.chat.android.core.internal.InternalStreamChatApi",
    )
}
