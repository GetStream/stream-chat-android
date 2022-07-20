package io.getstream.chat.docs.kotlin.client.docusaurus

import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * @see <a href="https://getstream.io/chat/docs/sdk/android/basics/core-concepts/#core-concepts">Core Concepts</a>
 */
class CoreConcepts {

    fun calls(channelClient: ChannelClient, message: Message) {
        val messageResult = channelClient.sendMessage(message).execute()
    }

    fun runningCallsAsynchronously(channelClient: ChannelClient, message: Message, viewModelScope: CoroutineScope) {
        // Safe to call from the main thread
        channelClient.sendMessage(message).enqueue { result: Result<Message> ->
            if (result.isSuccess) {
                val sentMessage = result.data()
            } else {
                // Handle result.error()
            }
        }

        viewModelScope.launch {
            // Safe to call from any CoroutineContext
            val messageResult = channelClient.sendMessage(message).await()
        }
    }

    fun errorHandling(result: Result<Channel>) {
        if (result.isSuccess) {
            // Use result.data()
        } else {
            // Handle result.error()
        }
    }
}
