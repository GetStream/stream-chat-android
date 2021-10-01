package io.getstream.chat.android.command.dag.unittest.plugin

import io.getstream.chat.android.command.dag.unittest.task.UnitTestsTask
import io.getstream.chat.android.command.utils.registerExt
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

private const val CONFIG_CLOJURE_NAME = "unitTestDebugScript"
private const val COMMAND_NAME = "test-debug"

class UnitTestsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension: UnitTestsCommandExtension =
            project.extensions.create(CONFIG_CLOJURE_NAME, UnitTestsCommandExtension::class.java)

        project.tasks.registerExt<UnitTestsTask>(COMMAND_NAME) {
            this.config = extension
        }
    }
}
