package io.getstream.chat.android.offline.experimental.plugin.state

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.experimental.querychannels.state.QueryChannelsMutableState
import io.getstream.chat.android.offline.experimental.querychannels.state.QueryChannelsState
import java.util.concurrent.ConcurrentHashMap

@InternalStreamChatApi
@ExperimentalStreamChatApi
public class StateRegistry internal constructor(
    private val chatDomainImpl: ChatDomainImpl,
    private val chatClient: ChatClient,
) {

    private val queryChannels: ConcurrentHashMap<Pair<FilterObject, QuerySort<Channel>>, QueryChannelsMutableState> =
        ConcurrentHashMap()

    public fun queryChannels(filter: FilterObject, sort: QuerySort<Channel>): QueryChannelsState {
        return queryChannels.getOrPut(filter to sort) {
            QueryChannelsMutableState(filter, sort, chatClient, chatDomainImpl.scope)
        }
    }

    public fun clear() {
        queryChannels.clear()
    }
}
