@file:Suppress("unused", "ControlFlowWithEmptyBody", "UNUSED_VARIABLE")

package io.getstream.chat.docs.kotlin.client.docusaurus

import android.content.Context
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.enqueue
import io.getstream.result.Result
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory

/**
 * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/offline-support">Offline Support</a>
 */
class OfflineSupport {

    private val apiKey = "api-key"

    fun configureOfflinePlugin(context: Context) {
        val offlinePluginFactory = StreamOfflinePluginFactory(appContext = context)

        ChatClient.Builder(apiKey, context)
            .withPlugins(offlinePluginFactory)
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
