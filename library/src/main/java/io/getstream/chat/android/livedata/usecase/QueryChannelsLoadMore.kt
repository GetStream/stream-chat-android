package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.Call2
import io.getstream.chat.android.livedata.CallImpl2
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.livedata.controller.QueryChannelsController

class QueryChannelsLoadMore(var domain: ChatDomain) {
    operator fun invoke (filter: FilterObject, sort: QuerySort?, limit: Int = 30, messageLimit: Int = 10): Call2<List<Channel>> {
        var runnable = suspend {
            val queryChannelsController = domain.queryChannels(filter, sort)
            queryChannelsController._loadMore(limit, messageLimit)
        }
        return CallImpl2<List<Channel>>(runnable, domain.scope)
    }
}