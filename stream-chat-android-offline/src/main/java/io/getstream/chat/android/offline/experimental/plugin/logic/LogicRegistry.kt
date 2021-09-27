package io.getstream.chat.android.offline.experimental.plugin.logic

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.experimental.plugin.state.StateRegistry
import io.getstream.chat.android.offline.experimental.querychannels.logic.QueryChannelLogic
import io.getstream.chat.android.offline.experimental.querychannels.logic.QueryChannelsLogic
import io.getstream.chat.android.offline.experimental.querychannels.state.toMutableState
import java.util.concurrent.ConcurrentHashMap

@ExperimentalStreamChatApi
internal class LogicRegistry internal constructor(private val stateRegistry: StateRegistry) {

    private val chatDomain: ChatDomainImpl
        get() = (ChatDomain.instance as ChatDomainImpl)

    private val queryChannels: ConcurrentHashMap<Pair<FilterObject, QuerySort<Channel>>, QueryChannelsLogic> =
        ConcurrentHashMap()

    fun queryChannels(filter: FilterObject, sort: QuerySort<Channel>): QueryChannelsLogic {
        return queryChannels.getOrPut(filter to sort) {
            QueryChannelsLogic(stateRegistry.queryChannels(filter, sort).toMutableState(), chatDomain)
        }
    }

    fun queryChannels(queryChannelsRequest: QueryChannelsRequest): QueryChannelsLogic =
        queryChannels(queryChannelsRequest.filter, queryChannelsRequest.querySort)

    fun queryChannel(): QueryChannelLogic = QueryChannelLogic(chatDomain)
}
