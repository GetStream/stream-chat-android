package io.getstream.chat.android.command.release.output

class InMemoryPrinter : Printer {
    private val lines: MutableList<String> = mutableListOf()

    override fun printline(text: String) {
        lines.add(text)
    }

    fun lines(): List<String> = lines
}
