package io.getstream.chat.android.command.release.markdown

import io.getstream.chat.android.command.release.model.Document
import io.getstream.chat.android.command.release.output.FilePrinter
import io.getstream.chat.android.command.release.output.print
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

fun createdUpdatedChangelog(fileName: String, modelFile: File, releaseDocument: Document, currentVersion: String) {
    FilePrinter(fileName).use { printer ->
        printer.modelHeader(modelFile)
        printer.printline("\n")

        printer.releaseHeader(releaseDocument, currentVersion)
    }
}

private fun FilePrinter.modelHeader(modelFile: File) {
    printline("# UNRELEASED CHANGELOG")
    modelFile.reader().copyTo(getPrinter())
}

private fun FilePrinter.releaseHeader(releaseDocument: Document, currentVersion: String) {
    printline(dateHeader(currentVersion))
    releaseDocument.print(this)
}

private fun dateHeader(currentVersion: String): String {
    val formattedDate = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).format(LocalDateTime.now())

    return "# $formattedDate - $currentVersion"
}
