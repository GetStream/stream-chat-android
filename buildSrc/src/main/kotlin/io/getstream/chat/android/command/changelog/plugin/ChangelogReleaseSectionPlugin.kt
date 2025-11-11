package io.getstream.chat.android.command.changelog.plugin

import io.getstream.chat.android.command.changelog.task.ChangelogReleaseSectionTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

private const val CONFIG_CLOJURE_NAME = "changelogReleaseSection"
private const val COMMAND_NAME = "changelog-release-section"

class ChangelogReleaseSectionPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension: ChangelogReleaseSectionCommandExtension =
            project.extensions.create(CONFIG_CLOJURE_NAME, ChangelogReleaseSectionCommandExtension::class.java)

        project.tasks.register<ChangelogReleaseSectionTask>(COMMAND_NAME) {
            this.config = extension
        }
    }
}
