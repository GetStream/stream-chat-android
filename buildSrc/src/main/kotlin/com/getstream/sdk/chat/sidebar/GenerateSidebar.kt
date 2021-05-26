package com.getstream.sdk.chat.sidebar

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileTree
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class GenerateSidebar : DefaultTask() {
    @get:InputDirectory
    abstract val inputDir: DirectoryProperty

    @get:InputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val modulesFilterInput: ListProperty<String>

    private val moshiAdapter =
        Moshi.Builder()
            .build()
            .adapter<Map<String, String>>(
                Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
            )

    init {
        group = "Docusaurus"
        description = "Generates _category_.json files for sidebar"
    }

    @TaskAction
    fun run() {
        val modulesToInclude = modulesFilterInput.get()
        val dokkaFileTree: FileTree = inputDir.asFileTree
        val outputFile = outputDir.get().asFile

        outputFile.listFiles()?.forEach { file -> file.deleteRecursively() }

        createSidebarFiles(dokkaFileTree, modulesToInclude)

        modulesToInclude.map { module ->
            Pair(
                File("${inputDir.get().asFile.path}/$module"),
                File("${outputFile.path}/$module")
            )
        }.forEach { (inputModule, outModule) ->
            inputModule.copyRecursively(outModule)
        }

        println("_category_.json files created")
    }

    private fun createSidebarFiles(fileTree: FileTree, modulesToInclude: List<String>) {
        fileTree.asSequence()
            .filter { file ->
                modulesToInclude.any { module ->
                    file.absolutePath.contains(module)
                }
            }
            .map { file -> file.parentFile }
            .distinct()
            .forEach(::createCategoryFile)
    }

    private fun createCategoryFile(parentFile: File) {
        val categoryFile = File("${parentFile.path}/_category.json")

        if (categoryFile.exists()) {
            categoryFile.delete()
        }

        val isCreated = categoryFile.createNewFile()

        if (isCreated) {
            categoryFile.writeText(categoryContent(parentFile.name))
        } else {
            println("Category file could not be created: $categoryFile")
        }
    }

    private fun categoryContent(label: String): String = moshiAdapter.toJson(mapOf("label" to label))
}
