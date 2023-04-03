package io.getstream.chat.docs.kotlin.client.docusaurus

import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.result.Result
import io.getstream.result.Error
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * @see <a href="https://getstream.io/chat/docs/sdk/android/basics/core-concepts/#core-concepts">Core Concepts</a>
 */
class CoreConcepts {

    fun calls(channelClient: ChannelClient, message: Message) {
        // Only call this from a background thread
        val messageResult = channelClient.sendMessage(message).execute()
    }

    fun runningCallsAsynchronously(channelClient: ChannelClient, message: Message, viewModelScope: CoroutineScope) {
        // Safe to call from the main thread
        channelClient.sendMessage(message).enqueue { result: Result<Message> ->
            when (result) {
                is Result.Success -> {
                    val sentMessage = result.value
                }
                is Result.Failure -> {
                    // Handler error
                }
            }
        }

        viewModelScope.launch {
            // Safe to call from any CoroutineContext
            val messageResult = channelClient.sendMessage(message).await()
        }
    }

    fun errorHandling(result: Result<Channel>) {
        when (result) {
            is Result.Success -> {
                val channel: Channel = result.value
                // Handle success
            }
            is Result.Failure -> {
                val error: Error = result.value
                // Handler error
            }
        }
    }

    fun errorHandlingReactively(result: Result<Channel>) {
        result.onSuccess { channel ->
            // Handle success
        }.onError { error ->
            // Handle error
        }
    }
}
