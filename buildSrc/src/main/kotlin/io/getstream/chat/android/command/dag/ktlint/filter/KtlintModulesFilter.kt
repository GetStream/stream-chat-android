package io.getstream.chat.android.command.dag.ktlint.filter

import org.gradle.api.Project
import org.gradle.api.plugins.PluginContainer

private fun PluginContainer.hasKtlint() = hasPlugin("org.jlleitschuh.gradle.ktlint")

private fun ktLintModules(iterable: Iterable<Project>): List<Project> {
    return iterable.filter { subProject -> subProject.plugins.hasKtlint() }
}

fun List<String>.filterKtlintModules(projects: Iterable<Project>): List<String> {
    val ktlintModules = ktLintModules(projects).map { project -> project.name }

    return this.filter(ktlintModules::contains)
}
