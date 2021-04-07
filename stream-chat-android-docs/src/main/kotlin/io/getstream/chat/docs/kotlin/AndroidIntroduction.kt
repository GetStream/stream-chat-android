package io.getstream.chat.docs.kotlin

import android.content.Context
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomain

class AndroidIntroduction {

    /**
     * @see <a href="https://getstream.io/chat/docs/android/?language=kotlin#chat-client">Chat Client</a>
     */
    fun chatClient(applicationContext: Context) {
        val apiKey = "{{ api_key }}"
        val token = "{{ chat_user_token }}"
        // Step 1 - Set up the client for API calls
        val client = ChatClient.Builder(apiKey, applicationContext)
            // Change log level
            .logLevel(ChatLogLevel.ALL)
            .build()
        // Step 2 - Set up the domain for offline storage
        val domain = ChatDomain.Builder(applicationContext, client)
            // Enable offline support
            .offlineEnabled()
            .build()

        // Step 2 - Authenticate and connect the user
        val user = User(
            id = "summer-brook-2",
            extraData = mutableMapOf(
                "name" to "Paranoid Android",
                "image" to "https://bit.ly/2TIt8NR",
            ),
        )
        client.connectUser(
            user = user,
            token = token, // or client.devToken(userId); if auth is disabled for your app
        ).enqueue { result ->
            if (result.isSuccess) {
                // Handle success
            } else {
                // Handler error
            }
        }
    }

    fun watchingAChannel(client: ChatClient, chatDomain: ChatDomain) {
        val channelClient = client.channel(channelType = "messaging", channelId = "travel")

        val extraData = mutableMapOf<String, Any>(
            "name" to "Awesome channel about traveling"
        )

        // Creating a channel with the low level client
        channelClient.create(extraData).enqueue { result ->
            if (result.isSuccess) {
                val channel: Channel = result.data()
                // Use channel by calling methods on channelClient
            } else {
                // Handle result.error()
            }
        }

        // Watching a channel's state using the offline library
        chatDomain.watchChannelCall(cid = "messaging:travel", messageLimit = 0)
            .enqueue { result ->
                if (result.isSuccess) {
                    val channelController = result.data()

                    // LiveData objects to observe
                    channelController.messages
                    channelController.reads
                    channelController.typing
                }
            }
    }

    fun sendFirstMessage(channelClient: ChannelClient, chatDomain: ChatDomain) {
        val message = Message(
            text = "I’m mowing the air Rand, I’m mowing the air.",
            cid = "messaging:travel",
            extraData = mutableMapOf("customField" to "123")
        )

        // Using the low level client
        channelClient.sendMessage(message).enqueue { result ->
            if (result.isSuccess) {
                val message: Message = result.data()
            } else {
                // Handle result.error()
            }
        }

        // Using the offline support library
        chatDomain.sendMessageCall(message).enqueue { result ->
            if (result.isSuccess) {
                val message: Message = result.data()
            } else {
                // Handle result.error()
            }
        }
    }

    fun queryChannels(client: ChatClient, chatDomain: ChatDomain) {
        val filter = Filters.and(
            Filters.eq("type", "messaging"),
            Filters.`in`("members", "john"),
        )
        val sort = QuerySort<Channel>().desc("last_message_at")

        // Using the low level client to query channels
        val request = QueryChannelsRequest(
            filter = filter,
            offset = 0,
            limit = 10,
            querySort = sort
        ).withWatch().withState()
        client.queryChannels(request).enqueue { result ->
            if (result.isSuccess) {
                val channels: List<Channel> = result.data()
            } else {
                // Handle result.error()
            }
        }

        // Using the offline library to query channels
        chatDomain.queryChannelsCall(filter, sort)
            .enqueue { result ->
                if (result.isSuccess) {
                    val queryChannelsController = result.data()

                    // LiveData objects to observe
                    queryChannelsController.channels
                    queryChannelsController.loading
                    queryChannelsController.endOfChannels
                } else {
                    // Handle result.error()
                }
            }
    }
}
