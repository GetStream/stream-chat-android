package io.getstream.chat.android.command.version.plugin

import io.getstream.chat.android.command.utils.registerExt
import io.getstream.chat.android.command.version.task.MinorBumpTask
import org.gradle.api.Plugin
import org.gradle.api.Project

private const val COMMAND_NAME = "minor-bump"

class MinorBumpPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.tasks.registerExt<MinorBumpTask>(COMMAND_NAME) {}
    }
}
