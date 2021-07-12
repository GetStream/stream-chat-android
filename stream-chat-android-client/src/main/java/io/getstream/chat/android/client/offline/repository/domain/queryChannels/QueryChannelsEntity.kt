package io.getstream.chat.android.client.offline.repository.domain.queryChannels

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.getstream.chat.android.client.api.models.FilterObject

@Entity(tableName = "stream_channel_query")
internal data class QueryChannelsEntity(
    @PrimaryKey
    var id: String,
    val filter: FilterObject,
    val cids: List<String>,
)
