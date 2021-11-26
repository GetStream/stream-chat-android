package io.getstream.chat.android.ui.utils

private const val MAX_LINE_BREAKS = 8

internal fun elipseText(text: String, textLimit: Int): String {
    return when {
        text.length > textLimit -> "${text.substring(0..textLimit)}..."

        textIsTooHeight(text) -> parseTooHeightText(text)

        else -> text
    }
}

private fun textIsTooHeight(text: String): Boolean {
    return text.count { char -> char == '\n' } > MAX_LINE_BREAKS
}

private fun parseTooHeightText(text: String): String {
    val stringBuilder = StringBuilder()
    var acc = 0
    var i = 0

    while (acc < MAX_LINE_BREAKS && i < text.length - 1) {
        val char = text[i]

        stringBuilder.append(char)

        if (char == '\n') acc++
        i++
    }

    stringBuilder.append("\n[...]")

    return stringBuilder.toString()
}
