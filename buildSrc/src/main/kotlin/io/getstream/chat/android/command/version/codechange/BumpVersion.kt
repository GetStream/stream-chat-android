package io.getstream.chat.android.command.version.codechange

import java.io.File

fun parseVersion(file: File, bumpMinor: Boolean): List<String> {
    val newLines = file.readLines().map { line ->
        when {
            line.trim().startsWith("const val minorVersion =") && bumpMinor -> bumpVersionInLine(line)

            line.trim().startsWith("const val patchVersion =") && !bumpMinor -> bumpVersionInLine(line)

            else -> line
        }
    }

    return newLines.toList()
}

private fun bumpVersionInLine(line: String): String {
    val lineArray = line.split("=")
    val newVersion = lineArray[1].trim().toInt().plus(1).toString()

    return "${lineArray[0]}= $newVersion"
}
