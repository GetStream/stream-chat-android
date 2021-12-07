package io.getstream.chat.android.offline.experimental.plugin.state

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.experimental.channel.state.ChannelMutableState
import io.getstream.chat.android.offline.experimental.channel.state.ChannelState
import io.getstream.chat.android.offline.experimental.channel.state.toMutableState
import io.getstream.chat.android.offline.experimental.channel.thread.state.ThreadMutableState
import io.getstream.chat.android.offline.experimental.channel.thread.state.ThreadState
import io.getstream.chat.android.offline.experimental.querychannels.state.QueryChannelsMutableState
import io.getstream.chat.android.offline.experimental.querychannels.state.QueryChannelsState
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap

@InternalStreamChatApi
@ExperimentalStreamChatApi
/** Registry of all state objects exposed in offline plugin. */
public class StateRegistry internal constructor(
    private val chatDomainImpl: ChatDomainImpl,
    private val chatClient: ChatClient,
) {
    private val queryChannels: ConcurrentHashMap<Pair<FilterObject, QuerySort<Channel>>, QueryChannelsMutableState> =
        ConcurrentHashMap()
    private val channels: ConcurrentHashMap<Pair<String, String>, ChannelMutableState> = ConcurrentHashMap()
    private val threads: ConcurrentHashMap<String, ThreadMutableState> = ConcurrentHashMap()

    public fun queryChannels(filter: FilterObject, sort: QuerySort<Channel>): QueryChannelsState {
        return queryChannels.getOrPut(filter to sort) {
            QueryChannelsMutableState(filter, sort, chatClient, chatDomainImpl.scope, chatDomainImpl.latestUsers)
        }
    }

    public fun channel(channelType: String, channelId: String): ChannelState {
        return channels.getOrPut(channelType to channelId) {
            ChannelMutableState(
                channelType,
                channelId,
                chatDomainImpl.scope,
                chatDomainImpl.user,
                chatDomainImpl.latestUsers
            )
        }
    }

    /** Returns [ThreadState] of thread replies with parent message that has id equal to [messageId]. */
    public fun thread(messageId: String): ThreadState {
        return threads.getOrPut(messageId) {
            val (channelType, channelId) = runBlocking {
                chatDomainImpl.repos.selectMessage(messageId)?.cid?.cidToTypeAndId()
                    ?: error("There is not such message with messageId = $messageId")
            }
            val channelsState = channel(channelType, channelId)
            ThreadMutableState(messageId, channelsState.toMutableState(), chatDomainImpl.scope)
        }
    }

    /** Clear state of all state objects. */
    public fun clear() {
        queryChannels.clear()
        channels.clear()
        threads.clear()
    }
}
