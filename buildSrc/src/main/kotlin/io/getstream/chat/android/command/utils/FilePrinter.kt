package io.getstream.chat.android.command.utils

import java.io.File
import java.io.Writer

fun writeFile(filePath: String, writeFunc: (Writer) -> Unit) {
    File(filePath).printWriter().use(writeFunc)
}
