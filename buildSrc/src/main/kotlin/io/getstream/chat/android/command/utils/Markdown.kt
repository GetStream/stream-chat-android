package io.getstream.chat.android.command.utils

import io.getstream.chat.android.command.release.markdown.clean
import io.getstream.chat.android.command.release.model.Document
import io.getstream.chat.android.command.release.model.Project
import io.getstream.chat.android.command.release.model.Section
import java.io.File

fun parseChangelogFile(file: File): Document {
    return file.readLines()
        .filterUnreleasedSection()
        .parseReleaseDocument()
        .clean()
}

fun filterOldReleases(file: File): List<String> {
    return file.readLines().filterOldReleases()
}

/**
 * Transforms the lines of the unreleased section in to a [Document].
 */
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

            line.isNotBlank() -> {
                currentSectionLines.add(line)
            }
        }
    }

    return document
}

/**
 * Filter all the old releases.
 */
private fun List<String>.filterOldReleases(): List<String> {
    var sectionCount = 0

    val filteredList = filter { line ->
        if (isStartOfMainSection(line)) {
            sectionCount++
        }

        sectionCount > 1
    }

    return filteredList.ifEmpty {
        throw IllegalStateException("Could not find the end unreleased section")
    }
}

fun isStartOfProject(line: String) = line.startsWith("##") && !line.startsWith("###")

fun isStartOfSection(line: String) = line.startsWith("###")

fun isStartOfMainSection(line: String) = line.trim().startsWith("# ")

/**
 * This method filters the only the part of the CHANGELOG that wasn't released yet.
 */
private fun List<String>.filterUnreleasedSection(): List<String> {
    var shouldAdd = false
    var firstSection = true

    val filteredList = filter { line ->
        when {
            firstSection && !shouldAdd && isStartOfMainSection(line) -> {
                shouldAdd = true
            }

            shouldAdd && isStartOfMainSection(line) -> {
                shouldAdd = false
                firstSection = false
            }
        }

        shouldAdd
    }

    return if (!shouldAdd && filteredList.isNotEmpty()) {
        filteredList
    } else {
        throw if (!shouldAdd) {
            IllegalStateException("Could not find the end of unreleased section")
        } else {
            IllegalStateException("Could not find the start of unreleased section")
        }
    }
}
