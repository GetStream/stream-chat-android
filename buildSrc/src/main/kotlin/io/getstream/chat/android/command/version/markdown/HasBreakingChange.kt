package io.getstream.chat.android.command.version.markdown

import io.getstream.chat.android.command.utils.UNRELEASED_END
import java.io.File

fun hasBreakingChange(file: File): Boolean {
    var hasBreakingChange = false

    file.useLines { lines ->
        lines.forEach { line ->
            if (line.contains(UNRELEASED_END))
                return hasBreakingChange

            if (line.contains("- \uD83D\uDEA8 Breaking change")) {
                hasBreakingChange = true
            }
        }
    }

    throw IllegalStateException("Could not reach the end of unreleased file")
}
