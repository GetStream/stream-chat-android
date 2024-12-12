package io.getstream.chat.android.command.unittest.filter

import io.getstream.chat.android.command.unittest.model.TestType
import io.getstream.chat.android.command.utils.generateGradleCommand
import org.gradle.api.Project
import java.io.File

fun List<String>.selectedUnitTestCommand(rootProject: Project): String {
    val modulesWithTest = filterModulesWithTests()

    return filterUnitTestableModules(rootProject)
        .filter { (testableModule, _) -> modulesWithTest.contains(testableModule) }
        .generateGradleCommand { (module, testType) ->
            "$module:${testType.testCommand} $module:${TestType.JACOCO_TEST_COVERAGE.testCommand}"
        }
}

private fun List<String>.filterModulesWithTests(): List<String> {
    return filter { module ->
        File("$module/src/test").exists()
    }
}

private fun List<String>.filterUnitTestableModules(rootProject: Project): List<Pair<String, TestType>> {
    val ktlintModules = rootProject.subprojects
        .filter { project -> project.hasUnitTest() && this.contains(project.name) }
        .map { project ->
            val testType = when {
                project.tasks.any { task -> task.name == TestType.ANDROID_LIBRARY_TEST.testCommand } -> {
                    TestType.ANDROID_LIBRARY_TEST
                }

                else -> TestType.JAVA_LIBRARY_TEST
            }

            project.name to testType
        }

    return ktlintModules
}

private fun Project.hasUnitTest(): Boolean = this.tasks.any { task ->
    task.name == TestType.ANDROID_LIBRARY_TEST.testCommand || task.name == TestType.JAVA_LIBRARY_TEST.testCommand
}
