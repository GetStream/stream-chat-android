package io.getstream.chat.android.command.release.output

/**
 * Printer that holds all the list in memory instead of printing then. Remember that this [Printer] can
 * consume a lot o memory.
 */
class InMemoryPrinter : Printer {
    private val lines: MutableList<String> = mutableListOf()

    override fun printline(text: String) {
        lines.add(text)
    }

    fun lines(): List<String> = lines
}
