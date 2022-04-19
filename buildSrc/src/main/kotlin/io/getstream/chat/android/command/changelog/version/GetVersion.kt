package io.getstream.chat.android.command.changelog.version

import io.getstream.chat.android.command.utils.MAJOR_VERSION_MARKER
import io.getstream.chat.android.command.utils.MINOR_VERSION_MARKER
import io.getstream.chat.android.command.utils.PATCH_VERSION_MARKER
import java.io.File

/**
 * Gets the current version of the SDK as a String
 */
fun getCurrentVersion(file: File): String {
    val currentVersion = file.readLines()
        .filter { line ->
            line.contains(MAJOR_VERSION_MARKER) ||
                line.contains(MINOR_VERSION_MARKER) ||
                line.contains(PATCH_VERSION_MARKER)
        }.joinToString(separator = ".") { line ->
            line.split("=")[1].trim()
        }

    return currentVersion
}
