package io.getstream.chat.android.command.release.model

import io.getstream.chat.android.command.release.model.Project

data class Document(val projects: MutableList<Project>): MutableList<Project> by projects
