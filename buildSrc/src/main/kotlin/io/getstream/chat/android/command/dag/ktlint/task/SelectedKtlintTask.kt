package io.getstream.chat.android.command.dag.ktlint.task

import io.getstream.chat.android.command.utils.generateGradleCommand
import io.getstream.chat.android.command.dag.ktlint.filter.filterKtlintModules
import io.getstream.chat.android.command.dag.ktlint.plugin.KtCommandExtesion
import io.getstream.chat.android.command.utils.parseModules
import io.getstream.chat.android.command.utils.changeModuleFileDoesNotExistInPath
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

open class SelectedKtlintTask : DefaultTask() {

    @Input
    lateinit var config: KtCommandExtesion

    @TaskAction
    private fun command() {
        val changedModulesFile = File(config.changeModulesPath)

        if (!changedModulesFile.exists()) {
            changeModuleFileDoesNotExistInPath(config.changeModulesPath)
        }

        val command = parseModules(changedModulesFile).ktlintCommand(project)
        File(config.outputPath).writeText(command)

        println("Command generated: $command")
        println("Command written in: ${config.outputPath}")
    }
}

fun List<String>.ktlintCommand(project: Project): String =
    filterKtlintModules(project.subprojects).generateGradleCommand { "ktlintCheck" }
