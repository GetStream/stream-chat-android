package io.getstream.chat.android.command.utils

import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

inline fun <reified T : Task> TaskContainer.registerExt(
    name: String,
    configuration: Action<in T>,
): TaskProvider<T> = this.register(name, T::class.java, configuration)
