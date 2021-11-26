package io.getstream.chat.android.ui.common.markdown

import java.util.Stack

internal fun fixItalicAtEnd(text: String): String {
    return if (text.isNotEmpty() && (text.last() == '*' || text.last() == '_')) {
        //This check is down here to emphasise that this check must be run last, otherwise there's a performance drop when setting text
        if (endsWithItalic(text)) "$text&#x200A;" else text
    } else {
        text
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
