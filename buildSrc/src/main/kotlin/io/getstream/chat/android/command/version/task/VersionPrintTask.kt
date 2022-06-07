package io.getstream.chat.android.command.version.task

import io.getstream.chat.android.command.changelog.version.getCurrentVersion
import io.getstream.chat.android.command.release.output.FilePrinter
import io.getstream.chat.android.command.version.plugin.VersionPrintCommandExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

open class VersionPrintTask: DefaultTask() {

    @Input
    lateinit var config: VersionPrintCommandExtension

    @ExperimentalStdlibApi
    @TaskAction
    private fun command() {
        val currentVersion = getCurrentVersion()

        FilePrinter(File(config.printFilePath)).use { printer ->
            printer.printline(currentVersion)
        }

        println("File: ${config.printFilePath} generated")
    }
}
