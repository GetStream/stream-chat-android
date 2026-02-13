package io.getstream.chat.docs.kotlin.dokka

import android.content.Context
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.logger.ChatLoggerHandler
import io.getstream.chat.android.models.User
import io.getstream.result.Result

/**
 * [DokkaRoot](https://getstream.github.io/stream-chat-android/)
 */
class DokkaRoot {

    fun buildClient(applicationContext: Context) {
        val apiKey = "{{ api_key }}"
        val token = "{{ chat_user_token }}"

        val client = ChatClient.Builder(apiKey, applicationContext).build()
    }

    fun connectUser(client: ChatClient, token: String) {
        val user = User(
            id = "summer-brook-2",
            name = "Paranoid Android",
            image = "https://bit.ly/2TIt8NR"
        )
        client.connectUser(
            user = user,
            token = token, // or client.devToken(userId); if auth is disabled for your app
        ).enqueue { result ->
            when (result) {
                is Result.Success -> {
                    // Handle success
                }
                is Result.Failure -> {
                    // Handler error
                }
            }
        }
    }

    fun logLevel(applicationContext: Context, apiKey: String) {
        val client = ChatClient.Builder(apiKey, applicationContext)
            // Change log level
            .logLevel(ChatLogLevel.ALL)
            .build()
    }

    fun loggerHandler(applicationContext: Context, apiKey: String) {
        val loggerHandler = object : ChatLoggerHandler {
            override fun logT(throwable: Throwable) { /* no-op */ }
            override fun logT(tag: Any, throwable: Throwable) { /* no-op */ }
            override fun logI(tag: Any, message: String) { /* no-op */ }
            override fun logD(tag: Any, message: String) { /* no-op */ }
            override fun logV(tag: Any, message: String) { /* no-op */ }
            override fun logW(tag: Any, message: String) { /* no-op */ }
            override fun logE(tag: Any, message: String) { /* no-op */ }
            override fun logE(tag: Any, message: String, throwable: Throwable) { /* no-op */ }
        }
        val client = ChatClient.Builder(apiKey, applicationContext)
            // Enable logs
            .logLevel(ChatLogLevel.ALL)
            // Provide loggerHandler instance
            .loggerHandler(loggerHandler)
            .build()
    }

}
