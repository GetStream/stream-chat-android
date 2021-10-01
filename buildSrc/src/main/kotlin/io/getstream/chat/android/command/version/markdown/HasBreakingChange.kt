package io.getstream.chat.android.command.version.markdown

import java.io.File

fun hasBreakingChange(file: File): Boolean {
    var hasBreakingChange = false

    file.useLines { lines ->
        lines.forEach { line ->
            println(line)

            if (line.contains("<!-- end of unreleased -->"))
                return hasBreakingChange

            if (line.contains("- \uD83D\uDEA8 Breaking change")) {
                hasBreakingChange = true
            }
        }
    }

    throw IllegalStateException("Could not reach the end of unreleased file")
}
