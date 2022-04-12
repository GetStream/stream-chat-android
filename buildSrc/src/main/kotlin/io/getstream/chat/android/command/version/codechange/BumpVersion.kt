package io.getstream.chat.android.command.version.codechange

import java.io.File

fun parseVersion(file: File, bumpMinor: Boolean): List<String> {
    var versionChanged = false
    val minorVersionMarker = "const val minorVersion ="
    val patchVersionMarker = "const val patchVersion ="

    val newLines = file.readLines().map { line ->
        val trimmedLine = line.trim()

        when {
            bumpMinor && trimmedLine.startsWith(minorVersionMarker) ->
                bumpVersionInLine(line).also {
                    versionChanged = true
                }

            bumpMinor && trimmedLine.startsWith(patchVersionMarker) -> {
                resetVersionInLine(line)
            }

            !bumpMinor && trimmedLine.startsWith(patchVersionMarker) ->
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
            "The script could not bump the version. Both marker \"$minorVersionMarker\" and marker \"$patchVersionMarker\" could not be found"
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
