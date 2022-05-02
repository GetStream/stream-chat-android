package io.getstream.chat.docs.kotlin.client.cms

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
import io.getstream.chat.android.offline.extensions.watchChannelAsState
import io.getstream.chat.android.offline.model.message.attachments.UploadAttachmentsNetworkType
import io.getstream.chat.android.offline.plugin.configuration.Config
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AndroidIntroduction {

    /**
     * @see <a href="https://getstream.io/chat/docs/android/?language=kotlin#chat-client">Chat Client</a>
     */
    fun chatClient(applicationContext: Context) {
        val apiKey = "{{ api_key }}"
        val token = "{{ chat_user_token }}"
        // Step 1 - Set up the OfflinePlugin for offline storage
        val offlinePluginFactory = StreamOfflinePluginFactory(
            config = Config(
                backgroundSyncEnabled = true,
                userPresence = true,
                persistenceEnabled = true,
                uploadAttachmentsNetworkType = UploadAttachmentsNetworkType.NOT_ROAMING,
            ),
            appContext = applicationContext,
        )

        // Step 2 - Set up the client, together with offline plugin, for API calls
        val client = ChatClient.Builder(apiKey, applicationContext)
            // Change log level
            .logLevel(ChatLogLevel.ALL)
            .withPlugin(offlinePluginFactory)
            .build()

        // Step 3 - Authenticate and connect the user
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

    /**
     * @see <a href="https://getstream.io/chat/docs/android/?language=kotlin#channels">Channels</a>
     */
    fun channels(client: ChatClient, scope: CoroutineScope) {
        val channelClient = client.channel(channelType = "messaging", channelId = "travel")

        val extraData = mutableMapOf<String, Any>(
            "name" to "Awesome channel about traveling"
        )

        // Creating a channel with the low level client
        channelClient.create(memberIds = emptyList(), extraData = extraData).enqueue { result ->
            if (result.isSuccess) {
                val channel: Channel = result.data()
                // Use channel by calling methods on channelClient
            } else {
                // Handle result.error()
            }
        }

        // Watching a channel's state using the offline library
        scope.launch {
            client.watchChannelAsState(cid = "messaging:travel", messageLimit = 0, forceRefresh = true)
                .collect { channelState ->
                    if (channelState != null) {
                        // StateFlow objects to observe
                        channelState.messages
                        channelState.reads
                        channelState.typing
                    } else {
                        // User not connected yet.
                    }
                }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/?language=kotlin#messages">Messages</a>
     */
    fun messages(channelClient: ChannelClient) {
        val message = Message(
            text = "I’m mowing the air Rand, I’m mowing the air.",
            cid = "messaging:travel",
            extraData = mutableMapOf("customField" to "123")
        )

        channelClient.sendMessage(message).enqueue { result ->
            if (result.isSuccess) {
                val message: Message = result.data()
            } else {
                // Handle result.error()
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/?language=kotlin#querying-channels">Querying Channels</a>
     */
    fun queryingChannels(client: ChatClient) {
        val filter = Filters.and(
            Filters.eq("type", "messaging"),
            Filters.`in`("members", "john"),
        )
        val sort = QuerySort<Channel>().desc("last_message_at")

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
    }
}
