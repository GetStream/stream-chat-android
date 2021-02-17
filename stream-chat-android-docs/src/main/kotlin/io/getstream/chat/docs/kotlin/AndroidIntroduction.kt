package io.getstream.chat.docs.kotlin

import android.content.Context
import com.getstream.sdk.chat.ChatUI
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomain

class AndroidIntroduction {

    /**
     * @see <a href="https://getstream.io/chat/docs/android/?language=kotlin#chat-client">Chat Client</a>
     */
    fun chatClient(applicationContext: Context) {
        val apiKey = "{{ api_key }}"
        val userToken = "{{ chat_user_token }}"
        // Step 1 - Set up the client for API calls
        val client = ChatClient.Builder(apiKey, applicationContext)
            // Change log level
            .logLevel(ChatLogLevel.ALL)
            .build()
        // Step 2 - Set up the domain for offline storage
        val domain = ChatDomain.Builder(client, applicationContext)
            // Enable offline support
            .offlineEnabled()
            .build()
        // Step 3 - Set up UI components
        val ui = ChatUI.Builder(applicationContext).build()

        // Step 2 - Authenticate and connect the user
        val user = User("summer-brook-2").apply {
            extraData["name"] = "Paranoid Android"
            extraData["image"] = "https://bit.ly/2TIt8NR"
        }
        client.connectUser(
            user = user,
            token = userToken // or client.devToken(userId); if auth is disabled for your app
        ).enqueue { result ->
            if (result.isSuccess) {
                // Handle success
            } else {
                // Handler error
            }
        }
    }
}
