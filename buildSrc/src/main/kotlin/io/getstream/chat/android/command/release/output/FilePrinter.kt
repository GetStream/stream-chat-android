package io.getstream.chat.android.command.release.output

import java.io.Closeable
import java.io.File

class FilePrinter(fileName: String) : Closeable, Printer {

    private val filePrinter = File(fileName).printWriter()

    override fun close() {
        filePrinter.close()
    }

    override fun printline(text: String) {
        filePrinter.println(text)
    }

}
