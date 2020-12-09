package io.getstream.chat.docs.kotlin

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.getTranslation

class Translation(val client: ChatClient) {

    /**
     * @see <a href="https://getstream.io/chat/docs/translation/?language=kotlin#message-translation-endpoint">Message Translation</a>
     */
    fun messageTranslation() {
        // Translate message to french
        val channelController = client.channel("messaging:general")
        val message = Message(text = "Hello, I would like to have more information about your product.")
        val frenchLanguage = "fr"
        channelController.sendMessage(message).enqueue { result ->
            val messageId = result.data().id
            client.translate(messageId, frenchLanguage).enqueue { translationResult ->
                val translatedMessage = translationResult.data()
                val translation = translatedMessage.getTranslation(frenchLanguage)
            }
        }
    }
}