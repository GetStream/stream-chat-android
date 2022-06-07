package io.getstream.chat.android.command.version.task

import io.getstream.chat.android.command.utils.hasBreakingChange
import io.getstream.chat.android.command.version.codechange.parseVersion
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

private const val CHANGELOG_PATH = "CHANGELOG.md"

open class VersionBumpTask: DefaultTask() {

    @ExperimentalStdlibApi
    @TaskAction
    private fun command() {
        val dependencyFile = File(CHANGELOG_PATH)
        val configurationFile = File(CONFIGURATION_PATH)

        val hasBreakingChange = hasBreakingChange(dependencyFile)
        val newInfo = parseVersion(configurationFile, hasBreakingChange)

        configurationFile.printWriter().use { printer ->
            newInfo.forEach(printer::println)
        }

        println("Breaking change: $hasBreakingChange")
        println("Version bumped.")
    }
}
