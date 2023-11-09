package io.getstream.chat.android.command.release.task

import io.getstream.chat.android.command.release.output.FilePrinter
import io.getstream.chat.android.command.release.output.print
import io.getstream.chat.android.command.release.plugin.ReleaseCommandExtension
import io.getstream.chat.android.command.utils.parseChangelogFile
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

open class ReleaseTask : DefaultTask() {

    @Input
    lateinit var config: ReleaseCommandExtension

    @TaskAction
    private fun command() {
        val changeLogFile = File(config.changelogPath)
        println("changelogPath: $changeLogFile")

        val releaseDocument = parseChangelogFile(changeLogFile)
        val outputFile = File(project.rootDir, "build/tmp/CHANGELOG_PARSED.md").also { it.parentFile.mkdirs() }
        FilePrinter(outputFile).use { printer -> releaseDocument.print(printer) }
        FilePrinter.fromFileName("CHANGELOG_PARSED.md").use { printer -> releaseDocument.print(printer) }

        println("CHANGELOG_PARSED.md generated")
    }
}
