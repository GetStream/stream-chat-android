package com.getstream.sdk.chat.sidebar.utils

import java.io.File
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyTo
import kotlin.io.path.writer

@ExperimentalPathApi
fun File.substituteLinesInFile(func: (line: String) -> String) {
    val tempFile = kotlin.io.path.createTempFile()

    tempFile.writer().use { writer ->
        forEachLine { line ->
            writer.appendLine(func(line))
        }
    }

    tempFile.copyTo(toPath(), overwrite = true)
}
