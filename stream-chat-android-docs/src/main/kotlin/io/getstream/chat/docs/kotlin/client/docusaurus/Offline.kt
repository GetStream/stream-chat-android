@file:Suppress("unused", "ControlFlowWithEmptyBody", "UNUSED_VARIABLE")

package io.getstream.chat.docs.kotlin.client.docusaurus

import android.content.Context
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.offline.extensions.getRepliesAsState
import io.getstream.chat.android.offline.extensions.globalState
import io.getstream.chat.android.offline.extensions.queryChannelsAsState
import io.getstream.chat.android.offline.extensions.state
import io.getstream.chat.android.offline.extensions.watchChannelAsState
import io.getstream.chat.android.offline.model.message.attachments.UploadAttachmentsNetworkType
import io.getstream.chat.android.offline.plugin.configuration.Config
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import io.getstream.chat.android.offline.plugin.state.channel.thread.ThreadState
import io.getstream.chat.android.offline.plugin.state.querychannels.QueryChannelsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

@InternalStreamChatApi
class Offline {

    private val apiKey = "api-key"

    private val scope = CoroutineScope(DispatcherProvider.IO)

    fun configureOfflinePlugin(context: Context) {
        val offlinePluginFactory = StreamOfflinePluginFactory(
            config = Config(
                backgroundSyncEnabled = true,
                userPresence = true,
                persistenceEnabled = true,
                uploadAttachmentsNetworkType = UploadAttachmentsNetworkType.NOT_ROAMING,
            ),
            appContext = context,
        )

        ChatClient.Builder(apiKey, context).withPlugin(offlinePluginFactory).build()
    }

    inner class State {
        val chatClient = ChatClient.instance()

        val filter = Filters.eq("type", "messaging")
        val sort = QuerySort.desc<Channel>("last_updated")
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

        fun accessStateWithApiCall() {
            // Returns StateFlow<QueryChannelsState?> object and performs queryChannels request
            val queryChannelsState: StateFlow<QueryChannelsState?> =
                chatClient.queryChannelsAsState(request = queryChannelsRequest, coroutineScope = scope)

            // Returns StateFlow<ChannelState?> object and performs watchChannel request
            val channelState: StateFlow<ChannelState?> =
                chatClient.watchChannelAsState(cid = "messaging:sampleId", messageLimit = 30, coroutineScope = scope)

            // Returns ThreadState object for a thread associated with a given parentMessageId
            val threadState: ThreadState =
                chatClient.getRepliesAsState(
                    messageId = "messaging:sampleId",
                    messageLimit = 30,
                    coroutineScope = scope
                )
        }
    }

    inner class Migration {
        val chatClient = ChatClient.instance()

        fun sendMessage() {
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

        fun updateMessage() {
            // Old approach
            /*
            val messageToUpdate = Message(text = "Updated text")
            ChatDomain.instance().editMessage(messageToUpdate).enqueue { result ->
                if (result.isSuccess) {
                    // Handle success
                } else {
                    // Handle error
                }
            }*/

            // New approach
            val messageToUpdate = Message(text = "Updated text")
            ChatClient.instance().updateMessage(messageToUpdate).enqueue { result ->
                if (result.isSuccess) {
                    // Handle success
                } else {
                    // Handle error
                }
            }
        }

        fun leaveChannel() {
            // Old approach
            /*ChatDomain.instance().leaveChannel(cid).enqueue { result ->
                if (result.isSuccess) {
                    // Handle success
                } else {
                    // Handle error
                }
            }*/

            val cid = "cid"
            // New approach
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
}
