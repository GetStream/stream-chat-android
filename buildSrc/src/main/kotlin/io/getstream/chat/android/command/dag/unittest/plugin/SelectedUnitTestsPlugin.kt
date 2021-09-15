package io.getstream.chat.android.command.dag.unittest.plugin

import io.getstream.chat.android.command.dag.unittest.task.SelectedUnitTestsTask
import io.getstream.chat.android.command.utils.registerExt
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

private const val CONFIG_CLOJURE_NAME = "selectedTestDebug"
private const val COMMAND_NAME = "selected-test-debug"

class SelectedUnitTestsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension: UnitTestsCommandExtesion =
            project.extensions.create(CONFIG_CLOJURE_NAME, UnitTestsCommandExtesion::class.java)

        project.tasks.registerExt<SelectedUnitTestsTask>(COMMAND_NAME) {
            this.config = extension
        }
    }
}
