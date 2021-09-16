package io.getstream.chat.android.command.utils

import com.squareup.moshi.adapter
import java.io.File

@ExperimentalStdlibApi
fun parseModules(modulesFile: File): List<String> {
    val json = modulesFile.readText()
    return moshi()
        .adapter<List<String>>()
        .fromJson(json)
        ?: emptyList()
}

fun List<String>.generateGradleCommand(commandFunction: (String) -> String): String {
    val command = joinToString(separator = " ") { module -> "$module:${commandFunction(module)}" }

    return "./gradlew $command"
}
