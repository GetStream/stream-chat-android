package io.getstream.chat.android.command.version.codechange

import java.io.File

fun parseVersion(file: File, bumpMinor: Boolean): List<String> {
    var versionChanged = false
    val minorVersionMarker = "const val minorVersion ="
    val patchVersionMarker = "const val minorVersion ="

    val newLines = file.readLines().map { line ->
        when {
            bumpMinor && line.trim().startsWith(minorVersionMarker) ->
                bumpVersionInLine(line).also {
                    versionChanged = true
                }

            !bumpMinor && line.trim().startsWith(patchVersionMarker) ->
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
