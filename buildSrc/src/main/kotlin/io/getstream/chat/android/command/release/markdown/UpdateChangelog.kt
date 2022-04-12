package io.getstream.chat.android.command.release.markdown

import io.getstream.chat.android.command.release.model.Document
import io.getstream.chat.android.command.release.output.Printer
import io.getstream.chat.android.command.release.output.print
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val DATE_PATTERN = "MMMM dd'th', yyyy"

fun createdUpdatedChangelog(
    printer: Printer,
    modelFile: File,
    releaseDocument: Document,
    oldReleases: List<String>,
    currentVersion: String,
) {
    printer.modelHeader(modelFile)
    printer.printline("")

    printer.releaseHeader(releaseDocument, currentVersion)

    printer.printOldReleases(oldReleases)
}

private fun Printer.modelHeader(modelFile: File) {
    printline("# UNRELEASED CHANGELOG")
    modelFile.readLines().forEach(this::printline)
}

private fun Printer.releaseHeader(releaseDocument: Document, currentVersion: String) {
    printline(dateHeader(currentVersion))
    releaseDocument.print(this)
}

private fun dateHeader(currentVersion: String): String {
    val formattedDate = DateTimeFormatter.ofPattern(DATE_PATTERN).format(LocalDateTime.now())

    return "# $formattedDate - $currentVersion"
}

private fun Printer.printOldReleases(oldReleases: List<String>) {
    oldReleases.forEach(this::printline)
}
