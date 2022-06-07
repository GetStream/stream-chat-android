package io.getstream.chat.android.command.changelog.task

import io.getstream.chat.android.command.changelog.plugin.ChangelogAddModelSectionCommandExtension
import io.getstream.chat.android.command.changelog.plugin.ChangelogReleaseSectionCommandExtension
import io.getstream.chat.android.command.release.markdown.addModelSection
import io.getstream.chat.android.command.release.output.InMemoryPrinter
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

open class ChangelogAddModelSectionTask : DefaultTask() {

    @Input
    lateinit var config: ChangelogAddModelSectionCommandExtension

    @TaskAction
    private fun command() {
        val changeLogFile = File(config.changelogPath)
        val inMemoryPrinter = InMemoryPrinter()

        println("changelogPath: $changeLogFile")

        addModelSection(
            inMemoryPrinter,
            File(config.changelogModel),
            changeLogFile.readLines()
        )

        changeLogFile.printWriter().use { printer ->
            inMemoryPrinter.lines().forEach(printer::println)
        }

        println("model section added in changelog")
    }
}
