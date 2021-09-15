package io.getstream.chat.android.command.dag.ktlint.task

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.plugins.PluginContainer
import org.gradle.api.tasks.TaskAction

open class SelectedKtlint : DefaultTask() {

    @TaskAction
    private fun command() {
        ktLintModules(project.subprojects)
    }
}

private fun ktLintModules(iterable: Iterable<Project>): List<Project> {
    return iterable.filter { subProject -> subProject.isModuleType() && subProject.plugins.hasKtlint() }
}

private fun PluginContainer.hasKtlint() = hasPlugin("org.jlleitschuh.gradle.ktlint")

private fun Project.isModuleType(): Boolean {
    val isLibrary = project.plugins.hasPlugin(PlugginType.Library.value)
    val isApplication = project.plugins.hasPlugin(PlugginType.Application.value)

    return isLibrary || isApplication
}

enum class ModuleType(val value: String) {
    Library("library"), Application("application"), All("all")
}

enum class PlugginType(val value: String) {
    Library("com.android.library"),
    Application("com.android.application")
}
