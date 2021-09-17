package io.getstream.chat.android.command.dag.unittest.filter

import io.getstream.chat.android.command.dag.unittest.model.TestType
import io.getstream.chat.android.command.utils.generateGradleCommand
import org.gradle.api.Project

fun List<String>.unitTestCommand(rootProject: Project): String {
    return filterUnitTestableModules(rootProject)
        .generateGradleCommand { (module, testType) -> "$module:${testType.testCommand}" }
}

private fun List<String>.filterUnitTestableModules(rootProject: Project): List<Pair<String, TestType>> {
    val ktlintModules = rootProject.subprojects
        .filter { project -> project.hasUnitTest() && this.contains(project.name) }
        .map { project ->
            val testType = if (project.tasks.any { task -> task.name == "testDebugUnitTest" }) {
                TestType.ANDROID_LIBRARY_TEST
            } else {
                TestType.JAVA_LIBRARY_TEST
            }

            project.name to testType
        }

    return ktlintModules
}

private fun Project.hasUnitTest(): Boolean = this.tasks.any { task ->
    task.name == "testDebugUnitTest" || task.name == "test"
}
