package io.getstream.chat.android.livedata.usecase

import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.Call2
import io.getstream.chat.android.livedata.CallImpl2
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.controller.QueryChannelsController
import kotlinx.coroutines.launch

interface QueryChannels {
    operator fun invoke(filter: FilterObject, sort: QuerySort? = null, limit: Int = 30, messageLimit: Int = 10): Call2<QueryChannelsController>
}

class QueryChannelsImpl(var domainImpl: ChatDomainImpl) : QueryChannels {
    override operator fun invoke(filter: FilterObject, sort: QuerySort?, limit: Int, messageLimit: Int): Call2<QueryChannelsController> {
        val queryChannelsControllerImpl = domainImpl.queryChannels(filter, sort)
        val queryChannelsController: QueryChannelsController = queryChannelsControllerImpl
        var runnable = suspend {
            if (limit > 0) {
                queryChannelsControllerImpl.scope.launch { queryChannelsControllerImpl.query(limit, messageLimit) }
            }
            Result(queryChannelsController, null)
        }
        return CallImpl2(runnable, queryChannelsControllerImpl.scope)
    }
}
