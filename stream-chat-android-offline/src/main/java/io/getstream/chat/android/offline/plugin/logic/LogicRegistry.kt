package io.getstream.chat.android.offline.plugin.logic

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.plugin.state.StateRegistry
import io.getstream.chat.android.offline.querychannels.logic.QueryChannelsLogic
import io.getstream.chat.android.offline.querychannels.state.toMutableState
import java.util.concurrent.ConcurrentHashMap

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
}
