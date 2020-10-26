package io.getstream.chat.android.livedata.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.utils.FilterObject
import java.util.Date
import java.util.Objects

@Entity(tableName = "stream_channel_query")
internal data class QueryChannelsEntity(var filter: FilterObject, val sort: QuerySort<Channel>) {
    @PrimaryKey
    var id: String

    init {
        // ugly hack to cleanup the filter object to prevent issues with filter object equality
        filter = FilterObject(filter.toMap())
        id = (Objects.hash(filter.toMap()) + Objects.hash(sort.toMap())).toString()
    }

    var channelCids: List<String> = listOf()

    /** we track when the query was created and updated so we can clear out old results */
    var createdAt: Date? = null
    var updatedAt: Date? = null
}
