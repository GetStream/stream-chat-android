package io.getstream.chat.android.command.dag.unittest.plugin

import io.getstream.chat.android.command.dag.unittest.task.SelectedUnitTestsTask
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

private const val CONFIG_CLOJURE_NAME = "testDebugSelected"
private const val COMMAND_NAME = "test-debug-selected"

class SelectedUnitTestsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension: UnitTestsCommandExtesion =
            project.extensions.create(CONFIG_CLOJURE_NAME, UnitTestsCommandExtesion::class.java)

        project.tasks.registerExt<SelectedUnitTestsTask>(COMMAND_NAME, Action {
            this.config = extension
        })
    }
}

inline fun <reified T : Task> TaskContainer.registerExt(
    name: String,
    configuration: Action<in T>,
): TaskProvider<T> = this.register(name, T::class.java, configuration)
