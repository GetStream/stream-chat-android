package io.getstream.chat.android.livedata.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.utils.FilterObject
import java.util.*

@Entity(tableName = "stream_channel_query")
data class QueryChannelsEntity(val filter: FilterObject, val sort: QuerySort? = null) {
    @PrimaryKey
    var id: String = (Objects.hash(filter) + Objects.hash(sort?.data)).toString()

    var channelCIDs: MutableList<String> = mutableListOf()

    /** we track when the query was created and updated so we can clear out old results */
    var createdAt: Date? = null
    var updatedAt: Date? = null
}