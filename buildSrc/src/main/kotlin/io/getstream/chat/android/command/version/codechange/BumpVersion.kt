package io.getstream.chat.android.command.version.codechange

import io.getstream.chat.android.command.utils.MINOR_VERSION_MARKER
import io.getstream.chat.android.command.utils.PATCH_VERSION_MARKER
import java.io.File

fun parseVersion(file: File, bumpMinor: Boolean): List<String> {
    var versionChanged = false

    val newLines = file.readLines().map { line ->
        val trimmedLine = line.trim()

        when {
            bumpMinor && trimmedLine.startsWith(MINOR_VERSION_MARKER) ->
                bumpVersionInLine(line).also {
                    versionChanged = true
                }

            bumpMinor && trimmedLine.startsWith(PATCH_VERSION_MARKER) -> {
                resetVersionInLine(line)
            }

            !bumpMinor && trimmedLine.startsWith(PATCH_VERSION_MARKER) ->
                bumpVersionInLine(line).also {
                    versionChanged = true
                }

            else -> line
        }
    }

    if (versionChanged) {
        return newLines.toList()
    } else {
        throw IllegalStateException(
            "The script could not bump the version. Both marker \"$MINOR_VERSION_MARKER\" and marker \"$PATCH_VERSION_MARKER\" could not be found"
        )
    }
}

private fun bumpVersionInLine(line: String): String {
    val lineArray = line.split("=")
    val newVersion = lineArray[1].trim().toInt().plus(1).toString()

    return "${lineArray[0]}= $newVersion"
}

private fun resetVersionInLine(line: String): String {
    val lineArray = line.split("=")

    return "${lineArray[0]}= 0"
}
