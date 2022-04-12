package io.getstream.chat.android.command.release.output

import java.io.Closeable
import java.io.File
import java.io.PrintWriter

class FilePrinter(file: File) : Closeable, Printer {

    private val filePrinter = file.printWriter()

    override fun close() {
        filePrinter.close()
    }

    override fun printline(text: String) {
        filePrinter.println(text)
    }

    companion object {
        fun fromFileName(fileName: String) = FilePrinter(File(fileName))
    }
}
