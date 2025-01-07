import io.getstream.chat.android.Dependencies
import io.getstream.chat.android.command.changelog.plugin.ChangelogReleaseSectionPlugin
import io.getstream.chat.android.command.changelog.task.ChangelogReleaseSectionTask
import io.getstream.chat.android.command.changelog.task.ChangelogAddModelSectionTask
import io.getstream.chat.android.command.release.plugin.ReleasePlugin
import io.getstream.chat.android.command.release.task.ReleaseTask
import io.getstream.chat.android.command.unittest.plugin.UnitTestsPlugin
import io.getstream.chat.android.command.unittest.task.UnitTestsTask
import io.getstream.chat.android.command.version.plugin.VersionBumpPlugin
import io.getstream.chat.android.command.version.plugin.MinorBumpPlugin
import io.getstream.chat.android.command.version.plugin.VersionPrintPlugin
import io.getstream.chat.android.command.version.task.VersionPrintTask
import io.getstream.chat.android.command.changelog.plugin.ChangelogAddModelSectionPlugin
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

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
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.shot) apply false
    alias(libs.plugins.androidx.navigation) apply false
    alias(libs.plugins.sonarqube) apply false
    id("io.getstream.chat.UnitTestsPlugin")
    id("io.getstream.chat.ReleasePlugin")
    id("io.getstream.chat.ChangelogReleaseSectionPlugin")
    id("io.getstream.chat.VersionBumpPlugin")
    id("io.getstream.chat.MinorBumpPlugin")
    id("io.getstream.chat.VersionPrintPlugin")
    id("io.getstream.chat.ChangelogAddModelSectionPlugin")
    alias(libs.plugins.gitversioner)
    alias(libs.plugins.gradle.versions)
    alias(libs.plugins.nexus.publish)
    alias(libs.plugins.binary.compatibility.validator)
    alias(libs.plugins.dokka)
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
apply(from = "${rootDir}/scripts/sonar.gradle")

subprojects {
    if (name != "stream-chat-android-docs"
            && buildFile.exists()) {
        apply(from = "${rootDir}/spotless/spotless.gradle")
    }
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(from = "${rootDir}/scripts/coverage.gradle")
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        Dependencies.isStable(currentVersion) && Dependencies.isNonStable(candidate.version)
    }
}

tasks.withType<VersionPrintTask> {
    config.printFilePath = "build/tmp/temp-version"
}

tasks.withType<UnitTestsTask> {
     config.outputPath = "build/tmp/unit-tests-command.sh"
}

tasks.withType<ReleaseTask> {
    config.changelogPath = "CHANGELOG.md"
}

tasks.withType<ChangelogReleaseSectionTask> {
    config.changelogPath = "CHANGELOG.md"
}

tasks.withType<ChangelogAddModelSectionTask> {
    config.changelogPath = "CHANGELOG.md"
    config.changelogModel = "CHANGELOG_MODEL.md"
}

tasks.withType<Delete> {
    delete(rootProject.layout.buildDirectory)
}

apiValidation {
    ignoredPackages.add("com/getstream/sdk/chat/databinding")
    ignoredPackages.add("io/getstream/chat/android/ui/databinding")

    ignoredProjects += listOf(
            "stream-chat-android-docs",
            "stream-chat-android-ui-components-sample",
            "stream-chat-android-test",
            "stream-chat-android-compose-sample",
            "stream-chat-android-ui-guides",
            "stream-chat-android-metrics",
    )

    nonPublicMarkers += listOf(
            "io.getstream.chat.android.core.internal.InternalStreamChatApi",
    )
}

apply(from = "${rootDir}/scripts/publish-root.gradle")
