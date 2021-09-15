package io.getstream.chat.android.command.dag.ktlint.task

import io.getstream.chat.android.command.dag.generateGradleCommand
import io.getstream.chat.android.command.dag.ktlint.filter.filterKtlintModules
import io.getstream.chat.android.command.dag.ktlint.plugin.KtCommandExtesion
import io.getstream.chat.android.command.dag.parseModules
import io.getstream.chat.android.command.utils.writeFile
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

open class SelectedKtlint : DefaultTask() {

    @Input
    lateinit var config: KtCommandExtesion

    @TaskAction
    private fun command() {
        val command = parseModules(File(config.changeModulesPath)).ktlintCommand(project)

        writeFile(config.outputPath) { writer ->
            writer.write(command)
        }

        println("Command written in: ${config.outputPath}")
    }
}

fun List<String>.ktlintCommand(project: Project): String =
    filterKtlintModules(project.subprojects).generateGradleCommand { "ktlintCheck" }
