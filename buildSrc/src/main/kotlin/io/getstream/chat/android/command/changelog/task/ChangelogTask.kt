package io.getstream.chat.android.command.changelog.task

import io.getstream.chat.android.command.changelog.plugin.ChangelogCommandExtension
import io.getstream.chat.android.command.changelog.version.getCurrentVersion
import io.getstream.chat.android.command.release.markdown.createdUpdatedChangelog
import io.getstream.chat.android.command.release.markdown.parseReleaseSectionInChangelog
import io.getstream.chat.android.command.release.output.InMemoryPrinter
import io.getstream.chat.android.command.utils.filterOldReleases
import io.getstream.chat.android.command.utils.parseChangelogFile
import io.getstream.chat.android.command.version.task.CONFIGURATION_PATH
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Task called to update an specified changelog. It takes most recent section and attributed a version to it.
 * Use this after the release is done.
 */
open class ChangelogTask : DefaultTask() {

    @Input
    lateinit var config: ChangelogCommandExtension

    @TaskAction
    private fun command() {
        val changeLogFile = File(config.changelogPath)
        val configurationFile = File(CONFIGURATION_PATH)

        println("changelogPath: $changeLogFile")

        val releaseDocument = parseChangelogFile(changeLogFile)
        val oldReleases = filterOldReleases(changeLogFile)
        val inMemoryPrinter = InMemoryPrinter()

        parseReleaseSectionInChangelog(
            inMemoryPrinter,
            File(config.changelogModel),
            releaseDocument,
            oldReleases,
            getCurrentVersion(configurationFile)
        )

        changeLogFile.printWriter().use { printer ->
            inMemoryPrinter.lines().forEach(printer::println)
        }

        println("changelog updated")
    }
}
