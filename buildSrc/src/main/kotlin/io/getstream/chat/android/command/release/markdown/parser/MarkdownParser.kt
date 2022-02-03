package io.getstream.chat.android.command.release.markdown.parser

import io.getstream.chat.android.command.release.model.Document
import io.getstream.chat.android.command.release.model.Project
import io.getstream.chat.android.command.release.model.Section
import io.getstream.chat.android.command.utils.UNRELEASED_END
import io.getstream.chat.android.command.utils.UNRELEASED_START
import java.io.File

fun parseChangelogFile(file: File): Document {
    return file.readLines()
        .filterListSection()
        .parseReleaseDocument()
}

private fun List<String>.parseReleaseDocument(): Document {
    val document = Document(mutableListOf())
    var currentProjectSections = mutableListOf<Section>()
    var currentSectionLines = mutableListOf<String>()

    this.forEachIndexed { i, line ->
        when {
            isStartOfProject(line) || i == this.lastIndex -> {
                if (i == this.lastIndex) {
                    currentSectionLines.add(line)
                }

                currentProjectSections.add(Section(currentSectionLines))
                document.add(Project(currentProjectSections))

                currentSectionLines = mutableListOf(line)
                currentProjectSections = mutableListOf()
            }

            isStartOfSection(line) -> {
                currentProjectSections.add(Section(currentSectionLines))
                currentSectionLines = mutableListOf(line)
            }

            line.startsWith("-") -> {
                currentSectionLines.add(line)
            }
        }
    }

    return document
}

fun isStartOfProject(line: String) = line.startsWith("##") && !line.startsWith("###")

fun isStartOfSection(line: String) = line.startsWith("###")

private fun List<String>.filterListSection(
    start: String = UNRELEASED_START,
    end: String = UNRELEASED_END
): List<String> {
    var shouldAdd = false

    val filteredList = filter { line ->
        if (line.trim() == start) {
            shouldAdd = true
        }

        if (line.trim() == end) {
            shouldAdd = false
        }

        shouldAdd
    }

    return if (!shouldAdd && filteredList.isNotEmpty()) {
        filteredList
    } else {
        throw IllegalStateException("Could not find the start or end of unreleased section")
    }
}
