package io.getstream.chat.android.ui.message.input.internal

/**
 * Provides a method to validate input text in the message composer.
 */
internal object MessageTextValidator {

    /**
     * Checks if the message text can be sent to the server.
     *
     * @return If the given message text can be sent.
     */
    fun isMessageTextValid(text: String): Boolean {
        return text.isNotBlank() && !isEmptyGiphy(text)
    }

    /**
     * Checks if the message text contains an incomplete "giphy" command.
     *
     * @return If the given message text contains an incomplete "giphy" command.
     */
    private fun isEmptyGiphy(text: String): Boolean {
        val giphyCommand = "/giphy"

        if (text.startsWith(giphyCommand)) {
            val giphyContent = text.removePrefix(giphyCommand)

            return giphyContent.isEmpty() || giphyContent.isBlank()
        }

        return false
    }
}
