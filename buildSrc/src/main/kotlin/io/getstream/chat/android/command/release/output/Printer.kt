package io.getstream.chat.android.command.release.output

import io.getstream.chat.android.command.release.model.Document
import io.getstream.chat.android.command.release.model.Section

interface Printer {
    fun printline(text: String)
}

fun Document.print(printer: Printer) {
    flatten().printline(printer)
}

fun List<Section>.printline(printer: Printer) {
    forEach { section ->
        section.forEach(printer::printline)
        printer.printline("")
    }
}
