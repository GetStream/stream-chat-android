package io.getstream.chat.android.markdown

import java.util.Stack

internal fun String.fixItalicAtEnd(): String {
    return if (this.isNotEmpty() && (this.last() == '*' || this.last() == '_')) {
        // This check is down here to emphasise that this check must be run last, otherwise there's a performance drop when setting text
        if (endsWithItalic(this)) "$this&#x200A;" else this
    } else {
        this
    }
}

private fun endsWithItalic(text: String): Boolean {
    val stack = Stack<Char>()
    text.forEach { char ->
        when {
            isItalicMarker(char) && (stack.isEmpty() || stack.peek() != char) -> {
                stack.push(char)
            }

            !stack.isEmpty() && stack.peek() == char -> {
                stack.pop()
            }
        }
    }

    return stack.empty()
}

private fun isItalicMarker(char: Char): Boolean {
    return char == '*' || char == '_'
}
