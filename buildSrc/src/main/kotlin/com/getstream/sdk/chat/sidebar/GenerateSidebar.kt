package com.getstream.sdk.chat.sidebar

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileTree
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyTo
import kotlin.io.path.writer

abstract class GenerateSidebar : DefaultTask() {
    @get:InputDirectory
    abstract val inputDir: DirectoryProperty

    @get:InputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val modulesFilterInput: ListProperty<String>

    @get:Input
    abstract val removeFromLabels: Property<String>

    @get:Input
    abstract val simplifyOutput: Property<Boolean>

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

    @ExperimentalPathApi
    @TaskAction
    fun run() {
        val modulesToInclude = modulesFilterInput.get()
        val dokkaFileTree: FileTree = inputDir.asFileTree
        val outputFile = outputDir.get().asFile
        val removeFromLabel = removeFromLabels.get()
        val simplifyOutput = simplifyOutput.get()

        println("Remove: $removeFromLabel")

        outputFile.listFiles()?.forEach { file -> file.deleteRecursively() }

        if (simplifyOutput) {
            simplifyFiles(dokkaFileTree, modulesToInclude)
        }

        // createSidebarFiles(dokkaFileTree, modulesToInclude, removeFromLabel)
        //
        // modulesToInclude.map { module ->
        //     Pair(
        //         File("${inputDir.get().asFile.path}/$module"),
        //         File("${outputFile.path}/$module")
        //     )
        // }.forEach { (inputModule, outModule) ->
        //     inputModule.copyRecursively(outModule)
        // }

        println("_category_.json files created")
    }

    @ExperimentalPathApi
    private fun simplifyFiles(fileTree: FileTree, modulesToInclude: List<String>) {
        val selectedModuleFiles = fileTree.asSequence()
            .filter { file ->
                modulesToInclude.any(file.absolutePath::contains)
            }

        selectedModuleFiles.filter { file -> !file.name.contains("index.md") }.forEach(File::delete)

        selectedModuleFiles.filter { file -> file.name.contains("index.md") }
            .forEach(::removeNotIndexLinksFromFile)
    }

    @ExperimentalPathApi
    private fun removeNotIndexLinksFromFile(file: File) {
        val tempFile = kotlin.io.path.createTempFile()

        tempFile.writer().use { writer ->
            file.forEachLine { line ->
                writer.appendLine(filterTagLinks(line))
            }
        }

        tempFile.copyTo(file.toPath(), overwrite = true)
    }

    private fun createSidebarFiles(fileTree: FileTree, modulesToInclude: List<String>, removeFromLabel: String) {
        fileTree.asSequence()
            .filter { file ->
                modulesToInclude.any { module ->
                    file.absolutePath.contains(module)
                }
            }
            .map { file -> file.parentFile }
            .distinct()
            .forEach { file ->
                createCategoryFile(file, removeFromLabel)
            }
    }

    private fun createCategoryFile(parentFile: File, removeFromLabel: String) {
        val categoryFile = File("${parentFile.path}/_category_.json")

        if (categoryFile.exists()) {
            categoryFile.delete()
        }

        val isCreated = categoryFile.createNewFile()

        if (isCreated) {
            categoryFile.writeText(categoryContent(parentFile.name.replace(removeFromLabel, "")))
        } else {
            println("Category file could not be created: $categoryFile")
        }
    }

    private fun categoryContent(label: String): String = moshiAdapter.toJson(mapOf("label" to label))
}
