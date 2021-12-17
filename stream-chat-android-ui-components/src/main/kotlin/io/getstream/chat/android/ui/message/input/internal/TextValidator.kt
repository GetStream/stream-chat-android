package io.getstream.chat.android.ui.message.input.internal

internal fun isMessageTextValid(text: String): Boolean {
    return text.isNotEmpty() && !emptyGiphy(text)
}

private fun emptyGiphy(text: String) : Boolean {
    val giphyCommand = "/giphy"

    if (text.startsWith(giphyCommand)) {
        val giphyContent = text.removePrefix(giphyCommand)

        return giphyContent.isEmpty() || giphyContent.isBlank()
    }

    return false
}

