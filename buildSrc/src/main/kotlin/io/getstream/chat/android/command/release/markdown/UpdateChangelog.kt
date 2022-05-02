package io.getstream.chat.android.command.release.markdown

import io.getstream.chat.android.command.release.model.Document
import io.getstream.chat.android.command.release.output.Printer
import io.getstream.chat.android.command.release.output.print
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val DATE_PATTERN = "MMMM dd'th', yyyy"

/**
 * Creates an updated version of the changelog using a [Printer].
 *
 * @param printer the [Printer] that will write the file.
 * @param modelFile the model file that will be used by developers to fill information during the development cycle.
 * @param releaseDocument the [Document] containing the release notes.
 * @param oldReleases all the old releases as a list of lines.
 * @param currentVersion the current version of the SDK.
 */
fun createdUpdatedChangelog(
    printer: Printer,
    modelFile: File,
    releaseDocument: Document,
    oldReleases: List<String>,
    currentVersion: String,
) {
    printer.modelHeader(modelFile)
    printer.printline("")

    printer.releaseSection(releaseDocument, currentVersion)

    printer.printOldReleases(oldReleases)
}

fun parseReleaseSectionInChangelog(
    printer: Printer,
    modelFile: File,
    releaseDocument: Document,
    oldReleases: List<String>,
    currentVersion: String,
) {
    printer.releaseSection(releaseDocument, currentVersion)
    printer.printOldReleases(oldReleases)
}

/**
 * Prints the header of the changelog. That's the unreleased section.
 */
private fun Printer.modelHeader(modelFile: File) {
    printline("# UNRELEASED CHANGELOG")
    modelFile.readLines().forEach(this::printline)
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
