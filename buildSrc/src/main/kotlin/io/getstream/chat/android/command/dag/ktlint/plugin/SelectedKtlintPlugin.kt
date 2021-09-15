package io.getstream.chat.android.command.dag.ktlint.plugin

import io.getstream.chat.android.command.dag.ktlint.task.SelectedKtlintTask
import io.getstream.chat.android.command.utils.registerExt
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

private const val CONFIG_CLOJURE_NAME = "selectedKtlint"
private const val COMMAND_NAME = "selected-ktlint"

class SelectedKtlintPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension: KtCommandExtesion = project.extensions.create(CONFIG_CLOJURE_NAME, KtCommandExtesion::class.java)

        project.tasks.registerExt<SelectedKtlintTask>(COMMAND_NAME) {
            this.config = extension
        }
    }
}
