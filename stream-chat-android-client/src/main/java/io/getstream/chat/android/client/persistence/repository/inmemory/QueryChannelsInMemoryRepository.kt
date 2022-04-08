package io.getstream.chat.android.client.persistence.repository.inmemory

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.QueryChannelsSpec
import io.getstream.chat.android.client.persistence.repository.QueryChannelsRepository

internal class QueryChannelsInMemoryRepository: QueryChannelsRepository {

    override suspend fun insertQueryChannels(queryChannelsSpec: QueryChannelsSpec) {
        TODO("Not yet implemented")
    }

    override suspend fun selectBy(filter: FilterObject, querySort: QuerySort<Channel>): QueryChannelsSpec? {
        TODO("Not yet implemented")
    }
}
