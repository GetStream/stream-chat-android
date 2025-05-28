package io.getstream.chat.android.command.release.markdown

import io.getstream.chat.android.command.release.model.Document
import io.getstream.chat.android.command.release.output.Printer
import io.getstream.chat.android.command.release.output.print
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val DATE_PATTERN = "MMMM dd'th', yyyy"

fun parseReleaseSectionInChangelog(
    printer: Printer,
    releaseDocument: Document,
    oldReleases: List<String>,
    currentVersion: String,
) {
    printer.releaseSection(releaseDocument, currentVersion)
    printer.printOldReleases(oldReleases)
}

/**
 * Prints the release section of the changelog.
 */
private fun Printer.releaseSection(releaseDocument: Document, currentVersion: String) {
    printline(dateHeader(currentVersion))
    releaseDocument.print(this)
}

/**
 * Returns the date section with the current version.
 */
private fun dateHeader(currentVersion: String): String {
    val formattedDate = DateTimeFormatter.ofPattern(DATE_PATTERN).format(LocalDateTime.now())

    return "# $formattedDate - $currentVersion"
}

/**
 * Prints all the old versions of the SDK.
 */
private fun Printer.printOldReleases(oldReleases: List<String>) {
    oldReleases.forEach(this::printline)
}
