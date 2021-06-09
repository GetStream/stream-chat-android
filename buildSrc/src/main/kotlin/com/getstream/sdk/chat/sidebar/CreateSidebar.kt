package com.getstream.sdk.chat.sidebar

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.gradle.api.file.FileTree
import java.io.File

private val moshiAdapter =
    Moshi.Builder()
        .build()
        .adapter<Map<String, String>>(
            Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
        )

fun createSidebarFiles(fileTree: FileTree, modulesToInclude: List<String>, removeFromLabel: String) {
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
