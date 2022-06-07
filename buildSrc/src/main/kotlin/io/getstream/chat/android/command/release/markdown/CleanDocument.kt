package io.getstream.chat.android.command.release.markdown

import io.getstream.chat.android.command.release.extensions.toDocument
import io.getstream.chat.android.command.utils.isStartOfProject
import io.getstream.chat.android.command.release.model.Document
import io.getstream.chat.android.command.release.model.Project
import io.getstream.chat.android.command.release.model.Section

internal fun Document.clean(): Document =
    removeEmptyProjects()
        .map { project -> project.removeEmptySections() }
        .toDocument()

internal fun Document.removeEmptyProjects(): List<Project> =
    filter { project ->
        project.flatten().any { line ->
            line.startsWith("-")
        }
    }

internal fun List<Section>.removeEmptySections(): List<Section> =
    filter { section ->
        section.any { line ->
            line.startsWith("-") || isStartOfProject(line)
        }
    }
