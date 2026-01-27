@file:Suppress("unused", "ControlFlowWithEmptyBody", "UNUSED_VARIABLE")

package io.getstream.chat.docs.kotlin.client.docusaurus

import android.content.Context
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.OfflineConfig
import io.getstream.result.Result

/**
 * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/offline-support">Offline Support</a>
 */
class OfflineSupport {

    private val apiKey = "api-key"

    fun configureOfflineSupport(context: Context) {
        val config = OfflineConfig(
            enabled = true,
            ignoredChannelTypes = setOf("livestream"),
        )

        ChatClient.Builder(apiKey, context)
            .offlineConfig(config)
            .build()
    }

    fun clearData(chatClient: ChatClient) {
        chatClient.disconnect(flushPersistence = true).enqueue { result ->
            when(result) {
                is Result.Success -> {
                    // Handle success
                }
                is Result.Failure -> {
                    // Handle error
                }
            }
        }
    }
}
