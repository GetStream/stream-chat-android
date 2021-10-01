package io.getstream.chat.android.command.version.plugin

import io.getstream.chat.android.command.utils.registerExt
import io.getstream.chat.android.command.version.task.VersionBumpTask
import org.gradle.api.Plugin
import org.gradle.api.Project

private const val CONFIG_CLOJURE_NAME = "versionBump"
private const val COMMAND_NAME = "version-bump"

class VersionBumpPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.tasks.registerExt<VersionBumpTask>(COMMAND_NAME) {}
    }
}
