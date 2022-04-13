package io.getstream.chat.android.command.changelog.task

import io.getstream.chat.android.command.changelog.plugin.ChangelogCommandExtension
import io.getstream.chat.android.command.release.markdown.createdUpdatedChangelog
import io.getstream.chat.android.command.release.output.InMemoryPrinter
import io.getstream.chat.android.command.utils.filterOldReleases
import io.getstream.chat.android.command.utils.parseChangelogFile
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

open class ChangelogTask : DefaultTask() {

    @Input
    lateinit var config: ChangelogCommandExtension

    @TaskAction
    private fun command() {
        val changeLogFile = File(config.changelogPath)

        println("changelogPath: $changeLogFile")

        val releaseDocument = parseChangelogFile(changeLogFile)
        val oldReleases = filterOldReleases(changeLogFile)
        val inMemoryPrinter = InMemoryPrinter()

        createdUpdatedChangelog(
            inMemoryPrinter,
            File(config.changelogModel),
            releaseDocument,
            oldReleases,
            "5.0.4"
        )

        changeLogFile.printWriter().use { printer ->
            inMemoryPrinter.lines().forEach(printer::println)
        }

        println("changelog updated")
    }
}
