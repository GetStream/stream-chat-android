package io.getstream.chat.android.command.utils

import io.getstream.chat.android.command.release.markdown.clean
import io.getstream.chat.android.command.release.model.Document
import io.getstream.chat.android.command.release.model.Project
import io.getstream.chat.android.command.release.model.Section
import java.io.File

private const val UNRELEASED_START = "<!-- UNRELEASED START -->"
private const val UNRELEASED_END = "<!-- UNRELEASED END -->"

fun parseChangelogFile(file: File): Document {
    return file.readLines()
        .filterListSection()
        .parseReleaseDocument()
        .clean()
}

fun filterOldReleases(file: File): List<String> {
    return file.readLines().filterOldReleases()
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

private fun List<String>.filterOldReleases(start: String = "<!-- UNRELEASED END -->"): List<String> {
    var shouldAdd = false

    val filteredList = filter { line ->
        if (line.trim() == start) {
            shouldAdd = true
        }

        shouldAdd
    }

    return filteredList.ifEmpty {
        throw IllegalStateException("Could not find the end unreleased section")
    }
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

fun hasBreakingChange(file: File): Boolean {
    var hasBreakingChange = false

    file.useLines { lines ->
        lines.forEach { line ->
            if (line.contains(UNRELEASED_END))
                return hasBreakingChange

            if (line.contains("- \uD83D\uDEA8 Breaking change")) {
                hasBreakingChange = true
            }
        }
    }

    throw IllegalStateException("Could not reach the end of unreleased file")
}

