package io.getstream.chat.android.command.unittest.task

import io.getstream.chat.android.command.unittest.filter.getUnitTestCommand
import io.getstream.chat.android.command.unittest.plugin.UnitTestsCommandExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

open class UnitTestsTask : DefaultTask() {

    @Input
    lateinit var config: UnitTestsCommandExtension

    @ExperimentalStdlibApi
    @TaskAction
    private fun command() {
        val command = getUnitTestCommand(rootProject = project)
        File(project.rootDir, config.outputPath)
            .also { it.parentFile.mkdirs() }
            .writeText(command)

        println("Command generated: $command")
        println("Command written in: ${config.outputPath}")
    }
}
