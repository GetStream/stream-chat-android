package io.getstream.chat.android.command.dag.unittest.filter

import io.getstream.chat.android.command.dag.generateGradleCommand
import org.gradle.api.Project

fun List<String>.unitTestCommand(rootProject: Project): String {
    return filterUnitTestableModules(rootProject).generateGradleCommand { "testDebug" }
}

private fun List<String>.filterUnitTestableModules(rootProject: Project): List<String> {
    val ktlintModules = rootProject.unitTestsModules().map { project -> project.name }

    return filter(ktlintModules::contains)
}

private fun Project.unitTestsModules() : List<Project> {
    return subprojects.filter { project -> project.hasUnitTest() }
}

private fun Project.hasUnitTest(): Boolean = this.tasks.any { task -> task.name == "testDebugUnitTest" }
