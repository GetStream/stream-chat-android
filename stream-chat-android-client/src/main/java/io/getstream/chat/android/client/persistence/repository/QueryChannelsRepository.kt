package io.getstream.chat.android.client.persistence.repository

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.QueryChannelsSpec

public interface QueryChannelsRepository {
    public suspend fun insertQueryChannels(queryChannelsSpec: QueryChannelsSpec)
    public suspend fun selectBy(filter: FilterObject, querySort: QuerySort<Channel>): QueryChannelsSpec?
}
