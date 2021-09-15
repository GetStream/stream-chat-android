package io.getstream.chat.android.command.dag.ktlint.plugin

import io.getstream.chat.android.command.dag.ktlint.task.SelectedKtlint
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

class SelectedKtlintPlugin: Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.registerExt<SelectedKtlint>("ktlintSelected", Action {})
    }
}

inline fun <reified T : Task> TaskContainer.registerExt(
    name: String,
    configuration: Action<in T>
): TaskProvider<T> = this.register(name, T::class.java, configuration)
