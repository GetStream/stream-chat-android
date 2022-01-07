package io.getstream.chat.android.command.release.output

class StdoutPrinter : Printer {
    override fun printline(text: String) {
        println(text)
    }
}
