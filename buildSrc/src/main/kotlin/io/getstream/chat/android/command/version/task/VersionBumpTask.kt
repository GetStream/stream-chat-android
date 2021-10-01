package io.getstream.chat.android.command.version.task

import io.getstream.chat.android.command.version.markdown.hasBreakingChange
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

private const val CONFIGURATION_PATH = "buildSrc/src/main/kotlin/io/getstream/chat/android/Configuration.kt"
private const val CHANGELOG_PATH = "CHANGELOG.md"

open class VersionBumpTask: DefaultTask() {

    @ExperimentalStdlibApi
    @TaskAction
    private fun command() {
        val dependencyFile = File(CHANGELOG_PATH)

        val hasBreakingChange = hasBreakingChange(dependencyFile)

        println("Result: $hasBreakingChange")
    }
}
