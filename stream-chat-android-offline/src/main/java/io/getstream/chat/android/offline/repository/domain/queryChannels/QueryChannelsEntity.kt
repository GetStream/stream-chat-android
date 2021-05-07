package io.getstream.chat.android.offline.repository.domain.queryChannels

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import io.getstream.chat.android.client.api.models.FilterObject

@Entity(tableName = "stream_channel_query")
internal data class QueryChannelsEntity(
    @PrimaryKey
    var id: String,
    val filter: FilterObject,
    val cids: List<String>
)

@Entity(tableName = "channel_sort_inner_entity")
internal data class ChannelSortInnerEntity(val name: String, val direction: Int, val queryId: String) {
    @PrimaryKey
    var id: String = name.hashCode().toString() + direction.hashCode().toString() + queryId
}

internal data class QueryChannelsWithSorts(
    @Embedded val query: QueryChannelsEntity,
    @Relation(parentColumn = "id", entityColumn = "queryId", entity = ChannelSortInnerEntity::class)
    val sortInnerEntities: List<ChannelSortInnerEntity>
)
