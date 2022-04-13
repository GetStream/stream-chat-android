package io.getstream.chat.android.command.release.task

import io.getstream.chat.android.command.release.markdown.clean
import io.getstream.chat.android.command.utils.parseChangelogFile
import io.getstream.chat.android.command.release.output.FilePrinter
import io.getstream.chat.android.command.release.output.print
import io.getstream.chat.android.command.release.plugin.ReleaseCommandExtension
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

        val document = parseChangelogFile(changeLogFile).clean()

        FilePrinter("CHANGELOG_PARSED.md").use { printer ->
            document.print(printer)
        }

        println("CHANGELOG_PARSED.md generated")
    }
}
