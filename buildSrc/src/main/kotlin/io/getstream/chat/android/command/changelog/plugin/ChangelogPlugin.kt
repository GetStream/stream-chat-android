package io.getstream.chat.android.command.changelog.plugin

import io.getstream.chat.android.command.changelog.task.ChangelogTask
import io.getstream.chat.android.command.utils.registerExt
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

private const val CONFIG_CLOJURE_NAME = "changelogScript"
private const val COMMAND_NAME = "update-changelog"

class ChangelogPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension: ChangelogCommandExtension =
            project.extensions.create(CONFIG_CLOJURE_NAME, ChangelogCommandExtension::class.java)

        project.tasks.registerExt<ChangelogTask>(COMMAND_NAME) {
            this.config = extension
        }
    }
}
