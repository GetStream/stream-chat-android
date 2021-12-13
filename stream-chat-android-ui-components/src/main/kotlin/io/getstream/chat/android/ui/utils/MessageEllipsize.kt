package io.getstream.chat.android.ui.utils

private const val MAX_LINE_BREAKS = 8
private const val ELLIPSIS = "..."

internal fun ellipsizeText(text: String, textLimit: Int, maxLineBreaks: Int = MAX_LINE_BREAKS): String {
    return when {
        text.length > textLimit -> "${text.substring(0..textLimit)}$ELLIPSIS"

        textIsTooTall(text, maxLineBreaks) -> parseTooTallText(text, maxLineBreaks)

        else -> text
    }
}

private fun textIsTooTall(text: String, maxLineBreaks: Int): Boolean {
    return text.count { char -> char == '\n' } > maxLineBreaks
}

private fun parseTooTallText(text: String, maxLineBreaks: Int): String {
    var acc = 0
    var i = 0

    return buildString {
        while (acc < maxLineBreaks && i < text.length - 1) {
            val char = text[i]

            append(char)

            if (char == '\n') acc++
            i++
        }

        appendLine(ELLIPSIS)
    }
}
