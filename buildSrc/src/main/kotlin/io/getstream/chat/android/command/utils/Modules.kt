package io.getstream.chat.android.command.utils

import com.squareup.moshi.Types
import java.io.File

fun parseModules(modulesFile: File): List<String> {
    val json = modulesFile.readText()
    return moshi()
        .adapter<List<String>>(Types.newParameterizedType(List::class.java, String::class.java))
        .fromJson(json)
        ?: emptyList()
}

fun List<String>.generateGradleCommand(commandFunction: (String) -> String): String {
    val command = joinToString(separator = " ") { module -> "$module:${commandFunction(module)}" }

    return "./gradlew $command"
}
