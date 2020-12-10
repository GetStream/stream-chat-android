package io.getstream.chat.docs.kotlin

import android.util.Log
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.getTranslation
import io.getstream.chat.docs.StaticInstances.TAG

class Translation(val client: ChatClient) {

    /**
     * @see <a href="https://getstream.io/chat/docs/translation/?language=kotlin#message-translation-endpoint">Message Translation</a>
     */
    fun messageTranslation() {
        // Translate message to French
        val channelClient = client.channel("messaging:general")
        val message = Message(text = "Hello, I would like to have more information about your product.")

        channelClient.sendMessage(message).enqueue { result ->
            if (result.isSuccess) {
                val messageId = result.data().id
                val frenchLanguage = "fr"

                client.translate(messageId, frenchLanguage).enqueue { translationResult ->
                    if (translationResult.isSuccess) {
                        val translatedMessage = translationResult.data()
                        val translation = translatedMessage.getTranslation(frenchLanguage)
                    } else {
                        Log.e(TAG, String.format("There was an error %s", result.error()), result.error().cause)
                    }
                }
            } else {
                Log.e(TAG, String.format("There was an error %s", result.error()), result.error().cause)
            }
        }
    }
}
