package io.getstream.chat.android.command.unittest.filter

import io.getstream.chat.android.command.unittest.model.TestType
import io.getstream.chat.android.command.utils.generateGradleCommand
import org.gradle.api.Project
import java.io.File

fun getUnitTestCommand(rootProject: Project): String {
    val modulesWithTest = rootProject.subprojects.filter { module ->
        File("${module.name}/src/test").exists()
    }
    return modulesWithTest
        .mapUnitTestableModules()
        .generateGradleCommand { (module, testType) ->
            "$module:${testType.testCommand} $module:${TestType.JACOCO_TEST_COVERAGE.testCommand}"
        }
}

private fun List<Project>.mapUnitTestableModules(): List<Pair<String, TestType>> =
    filter(Project::hasAnyTestCommand)
        .map { project ->
            val testType = when {
                project.tasks.any { task -> task.name == TestType.COMPOSE_LIBRARY_TEST.testCommand } -> {
                    TestType.COMPOSE_LIBRARY_TEST
                }

                project.tasks.any { task -> task.name == TestType.ANDROID_LIBRARY_TEST.testCommand } -> {
                    TestType.ANDROID_LIBRARY_TEST
                }

                else -> TestType.JAVA_LIBRARY_TEST
            }

            project.name to testType
        }

private fun Project.hasAnyTestCommand(): Boolean =
    tasks.any { task ->
        task.name in listOf(
            TestType.COMPOSE_LIBRARY_TEST.testCommand,
            TestType.ANDROID_LIBRARY_TEST.testCommand,
            TestType.JAVA_LIBRARY_TEST.testCommand
        )
    }
