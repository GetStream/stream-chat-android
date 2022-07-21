package io.getstream.chat.docs.kotlin.client.docusaurus

import android.content.Context
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.querysort.QuerySortByField
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.offline.extensions.globalState
import io.getstream.chat.android.offline.extensions.state
import io.getstream.chat.android.offline.extensions.watchChannelAsState
import io.getstream.chat.android.offline.model.message.attachments.UploadAttachmentsNetworkType
import io.getstream.chat.android.offline.plugin.configuration.Config
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow

/**
 * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/chatdomain-migration/">ChatDomain Migration</a>
 */
class ChatDomainMigration {
    private val apiKey = "api-key"

    private val scope = CoroutineScope(Dispatchers.IO)
    val chatClient = ChatClient.instance()

    fun initialization(context: Context) {
        val offlinePluginFactory = StreamOfflinePluginFactory(
            config = Config(
                backgroundSyncEnabled = true,
                userPresence = true,
                persistenceEnabled = true,
                uploadAttachmentsNetworkType = UploadAttachmentsNetworkType.NOT_ROAMING,
                useSequentialEventHandler = false,
            ),
            appContext = context,
        )

        ChatClient.Builder(apiKey, context).withPlugin(offlinePluginFactory).build()
    }

    fun requestingData() {
        val cid = "channel-cid"
        val messageText = "Hey there!"

        val message = Message(cid = cid, text = messageText)

        // Old approach
        /*
        chatDomain.sendMessage(message).enqueue { result ->
            if (result.isSuccess) {
                // Handle success
            } else {
                // Handler error
            }
        }
        */

        // New approach
        chatClient.channel(cid).sendMessage(message).enqueue { result ->
            if (result.isSuccess) {
                // Handle success
            } else {
                // Handle error
            }
        }
    }

    inner class State {
        val chatClient = ChatClient.instance()

        val filter = Filters.eq("type", "messaging")
        val sort = QuerySortByField.descByName<Channel>("lastUpdated")
        private val queryChannelsRequest = QueryChannelsRequest(filter = Filters.eq("type", "messaging"), limit = 30)

        fun accessStates() {
            // Returns QueryChannelsState object based on filter and sort used to query channels
            val queryChannelsState = chatClient.state.queryChannels(filter = filter, sort = sort)

            // Returns ChannelState object for a given channel
            val channelState = chatClient.state.channel(channelType = "messaging", channelId = "sampleId")

            // Returns ThreadState object for a thread associated with a given parentMessageId
            val threadState = chatClient.state.thread(messageId = "parentMessageId")

            // Gives you access to GlobalState object
            val globalState = chatClient.globalState
        }

        fun requestChannelsAndObserveTheState() {
            // 1. Get the first 30 channels to which thierry belongs
            val filter = Filters.`in`("members", "thierry")
            val sort = QuerySortByField.descByName<Channel>("lastUpdated")
            val request = QueryChannelsRequest(
                filter = filter,
                querySort = sort,
                limit = 30,
                offset = 0,
                messageLimit = 1,
                memberLimit = 30,
            )
            ChatClient.instance().queryChannels(request).enqueue { result ->
                if (result.isSuccess) {
                    // Request successful. Data will be propagated to the state object
                } else {
                    // Handle error
                }
            }
            // 2. Get the state object associated with the above API call
            val queryChannelsState = ChatClient.instance().state.queryChannels(filter = filter, sort = sort)
        }

        fun watchChannel() {
            // Old approach - returns ChannelController object and performs watchChannel request
            // ChatDomain.instance().watchChannel(cid = "messaging:sampleId", messageLimit = 30).enqueue { result ->
            //     if (result.isSuccess) {
            //         val channelController = result.data()
            //     } else {
            //         // Handle error
            //     }
            // }

            // New approach - returns the StateFlow<ChannelState?> object and performs watchChannel request
            val channelState: StateFlow<ChannelState?> = chatClient.watchChannelAsState(
                cid = "messaging:sampleId",
                messageLimit = 30,
                coroutineScope = scope,
            )
        }
    }

    fun otherChanges() {
        // Old approach of update message
        /*
        val messageToUpdate = Message(text = "Updated text")
        ChatDomain.instance().editMessage(messageToUpdate).enqueue { result ->
            if (result.isSuccess) {
                // Handle success
            } else {
                // Handle error
            }
        }*/

        // New approach of updating message
        val messageToUpdate = Message(text = "Updated text")
        ChatClient.instance().updateMessage(messageToUpdate).enqueue { result ->
            if (result.isSuccess) {
                // Handle success
            } else {
                // Handle error
            }
        }

        // Old approach of leaving channel
        /*ChatDomain.instance().leaveChannel(cid).enqueue { result ->
            if (result.isSuccess) {
                // Handle success
            } else {
                // Handle error
            }
        }*/

        val cid = "cid"
        // New approach of leaving channel
        chatClient.getCurrentUser()?.let { currentUser ->
            ChatClient.instance().channel(cid).removeMembers(listOf(currentUser.id)).enqueue { result ->
                if (result.isSuccess) {
                    // Handle success
                } else {
                    // Handle error
                }
            }
        }
    }
}
