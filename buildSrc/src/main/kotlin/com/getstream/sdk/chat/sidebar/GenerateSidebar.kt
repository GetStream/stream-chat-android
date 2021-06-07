package com.getstream.sdk.chat.sidebar

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

@ExperimentalPathApi
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

    init {
        group = "Docusaurus"
        description = "Generates _category_.json files for sidebar"
    }

    @ExperimentalPathApi
    @TaskAction
    fun run() {
        val modulesToInclude: List<String> = modulesFilterInput.get()
        val dokkaFileTree: FileTree = inputDir.asFileTree
        val inputFile: File = inputDir.get().asFile
        val outputFile: File = outputDir.get().asFile
        val removeFromLabel: String = removeFromLabels.get()
        val simplifyOutput: Boolean = simplifyOutput.get()

        println("Remove: $removeFromLabel")

        outputFile.listFiles()?.forEach { file -> file.deleteRecursively() }

        if (simplifyOutput) {
            simplifyFiles(dokkaFileTree, modulesToInclude)
        }

        createSidebarFiles(dokkaFileTree, modulesToInclude, removeFromLabel)
        fixDirectoriesForMultiModule(modulesToInclude, inputFile)
        copyDirectories(modulesToInclude, inputFile, outputFile)

        println("_category_.json files created")
    }

    private fun fixDirectoriesForMultiModule(modulesToInclude: List<String>, inputDir: File) {
        modulesToInclude.map { module ->
            Triple(
                module,
                File("${inputDir.path}/$module/index.md"),
                File("${inputDir.path}/$module/$module/index.md")
            )
        }.forEach { (module, indexFile, destinationFile) ->
            fixIndexFile(indexFile, module)
            indexFile.copyTo(destinationFile)
        }
    }

    private fun fixIndexFile(indexFile: File, module: String) {
        indexFile.substituteInFile { line -> line.replace("$module/", "") }
    }

    private fun copyDirectories(modulesToInclude: List<String>, inputDir: File, outputFile: File) {
        modulesToInclude.map { module ->
            Pair(
                File("${inputDir.path}/$module/$module/"),
                File("${outputFile.path}/$module")
            )
        }.forEach { (inputModule, outModule) ->
            inputModule.copyRecursively(outModule)
        }
    }

    private fun simplifyFiles(fileTree: FileTree, modulesToInclude: List<String>) {
        val selectedModuleFiles = fileTree.asSequence()
            .filter { file ->
                modulesToInclude.any(file.absolutePath::contains)
            }

        selectedModuleFiles.filter { file -> !file.name.contains("index.md") }.forEach(File::delete)

        selectedModuleFiles.filter { file -> file.name.contains("index.md") }
            .forEach(::removeNotIndexLinksFromFile)
    }

    private fun removeNotIndexLinksFromFile(file: File) {
        file.substituteInFile(::filterTagLinks)
    }

    private fun File.substituteInFile(func: (String) -> String) {
        val tempFile = kotlin.io.path.createTempFile()

        tempFile.writer().use { writer ->
            forEachLine { line ->
                writer.appendLine(func(line))
            }
        }

        tempFile.copyTo(toPath(), overwrite = true)
    }
}
