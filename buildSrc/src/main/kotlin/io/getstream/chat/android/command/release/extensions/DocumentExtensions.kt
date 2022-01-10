package io.getstream.chat.android.command.release.extensions

import io.getstream.chat.android.command.release.model.Document
import io.getstream.chat.android.command.release.model.Project
import io.getstream.chat.android.command.release.model.Section

fun List<List<Section>>.toDocument(): Document =
    this.map { sections ->
        Project(sections.toMutableList())
    }.let { projects ->
        Document(projects.toMutableList())
    }
