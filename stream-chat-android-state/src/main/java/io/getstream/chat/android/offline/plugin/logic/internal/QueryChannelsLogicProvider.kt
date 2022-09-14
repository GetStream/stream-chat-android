package io.getstream.chat.android.offline.plugin.logic.internal

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.querysort.QuerySorter
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.offline.plugin.logic.querychannels.internal.QueryChannelsLogic

internal interface QueryChannelsLogicProvider {

    fun queryChannels(filter: FilterObject, sort: QuerySorter<Channel>): QueryChannelsLogic

    fun queryChannels(queryChannelsRequest: QueryChannelsRequest): QueryChannelsLogic
}
