package io.getstream.chat.android.offline.repository.domain.queryChannels

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel

@Entity(tableName = "stream_channel_query")
internal data class QueryChannelsEntity(
    @PrimaryKey
    var id: String,
    val filter: FilterObject,
    val querySort: QuerySort<Channel>,
    val cids: List<String>
)
