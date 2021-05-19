package com.getstream.sdk.chat.sidebar

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.asSequence

abstract class GenerateSidebar : DefaultTask() {
    @get:Input
    abstract val dokkaPathInput: Property<String>

    init {
        group = "Docusaurus"
        description = "Generates _category_.json files for sidebar"
    }

    @TaskAction
    fun run() {
        val dokkaPath: Path = Paths.get(dokkaPathInput.get())

        println("Dokka directory: ${dokkaPath.toAbsolutePath()}")

        println("Files: ")
        Files.list(dokkaPath).asSequence().forEach { path ->
            println(path.toString())
        }
    }
}