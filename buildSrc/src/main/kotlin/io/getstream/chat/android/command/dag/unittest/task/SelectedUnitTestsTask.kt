package io.getstream.chat.android.command.dag.unittest.task

import io.getstream.chat.android.command.dag.parseModules
import io.getstream.chat.android.command.dag.unittest.filter.unitTestCommand
import io.getstream.chat.android.command.dag.unittest.plugin.UnitTestsCommandExtesion
import io.getstream.chat.android.command.utils.changeModuleFileDoesNotExistInPath
import io.getstream.chat.android.command.utils.writeFile
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

open class SelectedUnitTestsTask : DefaultTask() {

    @Input
    lateinit var config: UnitTestsCommandExtesion

    @TaskAction
    private fun command() {
        val changedModulesFile = File(config.changeModulesPath)

        if (!changedModulesFile.exists()) {
            changeModuleFileDoesNotExistInPath(config.changeModulesPath)
        }

        val command = parseModules(changedModulesFile).unitTestCommand(project)

        writeFile(config.outputPath) { writer ->
            writer.write(command)
        }

        println("Command generated: $command")
        println("Command written in: ${config.outputPath}")
    }
}
