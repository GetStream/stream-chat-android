@file:Suppress("unused", "ControlFlowWithEmptyBody", "UNUSED_VARIABLE")

package io.getstream.chat.docs.kotlin.client.docusaurus

import android.content.Context
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.querysort.QuerySortByField
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.UploadAttachmentsNetworkType
import io.getstream.chat.android.state.extensions.getRepliesAsState
import io.getstream.chat.android.state.extensions.globalState
import io.getstream.chat.android.state.extensions.queryChannelsAsState
import io.getstream.chat.android.state.extensions.state
import io.getstream.chat.android.state.extensions.watchChannelAsState
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import io.getstream.chat.android.state.plugin.state.channel.thread.ThreadState
import io.getstream.chat.android.state.plugin.state.querychannels.QueryChannelsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow

/**
 * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/offline-support">Offline Support</a>
 */
class Offline {

    private val apiKey = "api-key"

    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/offline-support/#configuration">Configuration</a>
     */
    fun configureOfflinePlugin(context: Context) {
        val offlinePluginFactory = StreamOfflinePluginFactory(appContext = context)

        ChatClient.Builder(apiKey, context)
            .withPlugins(offlinePluginFactory)
            .uploadAttachmentsNetworkType(UploadAttachmentsNetworkType.NOT_ROAMING)
            .build()
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/offline-support/#state">State</a>
     */
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
}
