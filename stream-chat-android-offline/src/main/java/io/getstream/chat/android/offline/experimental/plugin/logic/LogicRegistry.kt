package io.getstream.chat.android.offline.experimental.plugin.logic

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.experimental.channel.logic.ChannelLogic
import io.getstream.chat.android.offline.experimental.channel.state.toMutableState
import io.getstream.chat.android.offline.experimental.channel.thread.logic.ThreadLogic
import io.getstream.chat.android.offline.experimental.channel.thread.state.toMutableState
import io.getstream.chat.android.offline.experimental.plugin.state.StateRegistry
import io.getstream.chat.android.offline.experimental.querychannels.logic.QueryChannelsLogic
import io.getstream.chat.android.offline.experimental.querychannels.state.toMutableState
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap

@ExperimentalStreamChatApi
/**
 * Registry-container for logic objects related to:
 * 1. Query channels
 * 2. Query channel
 * 3. Query thread
 */
internal class LogicRegistry internal constructor(private val stateRegistry: StateRegistry) {

    private val chatDomain: ChatDomainImpl
        get() = (ChatDomain.instance as ChatDomainImpl)

    private val queryChannels: ConcurrentHashMap<Pair<FilterObject, QuerySort<Channel>>, QueryChannelsLogic> =
        ConcurrentHashMap()
    private val channels: ConcurrentHashMap<Pair<String, String>, ChannelLogic> = ConcurrentHashMap()
    private val threads: ConcurrentHashMap<String, ThreadLogic> = ConcurrentHashMap()

    fun queryChannels(filter: FilterObject, sort: QuerySort<Channel>): QueryChannelsLogic {
        return queryChannels.getOrPut(filter to sort) {
            QueryChannelsLogic(stateRegistry.queryChannels(filter, sort).toMutableState(), chatDomain)
        }
    }

    /** Returns [QueryChannelsLogic] accordingly to [QueryChannelsRequest]. */
    fun queryChannels(queryChannelsRequest: QueryChannelsRequest): QueryChannelsLogic =
        queryChannels(queryChannelsRequest.filter, queryChannelsRequest.querySort)

    /** Returns [ChannelLogic] by channelType and channelId combination. */
    fun channel(channelType: String, channelId: String): ChannelLogic {
        return channels.getOrPut(channelType to channelId) {
            ChannelLogic(stateRegistry.channel(channelType, channelId).toMutableState(), chatDomain)
        }
    }

    /** Returns [ThreadLogic] of thread replies with parent message that has id equal to [messageId]. */
    fun thread(messageId: String): ThreadLogic {
        return threads.getOrPut(messageId) {
            val (channelType, channelId) = runBlocking {
                chatDomain.repos.selectMessage(messageId)?.cid?.cidToTypeAndId()
                    ?: error("There is not such message with messageId = $messageId")
            }
            ThreadLogic(stateRegistry.thread(messageId).toMutableState(), channel(channelType, channelId))
        }
    }

    fun clear() {
        queryChannels.clear()
        channels.clear()
        threads.clear()
    }
}
