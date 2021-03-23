package io.getstream.chat.docs.cookbook.ui

import android.content.Context
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.sdk.chat.ChatUI

/**
 * @see <a href="https://github.com/GetStream/stream-chat-android/wiki/UI-Cookbook#setup">Setup</a>
 */
class Setup {

    fun initializeSdk(applicationContext: Context) {
        val client = ChatClient.Builder(apiKey = "apiKey", appContext = applicationContext)
            .logLevel(ChatLogLevel.ALL)
            .build()

        val domain = ChatDomain.Builder(applicationContext, client)
            .offlineEnabled()
            .build()

        val ui = ChatUI.Builder(appContext = applicationContext).build()
    }

    fun connectUser() {
        val user = User(
            id = "bender",
            extraData = mutableMapOf(
                "name" to "Bender",
                "image" to "https://bit.ly/321RmWb",
            ),
        )

        ChatClient.instance().connectUser(user = user, token = "userToken")
            .enqueue { result ->
                if (result.isSuccess) {
                    // Handle success
                } else {
                    // Handle error
                }
            }
    }
}
