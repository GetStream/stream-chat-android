package com.getstream.sdk.chat.livedata.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.utils.FilterObject
import java.util.*

@Entity(tableName = "stream_channel_query")
data class QueryChannelsEntity(val filter: FilterObject) {

    @PrimaryKey
    var id: String

    var channelCIDs: List<String>


    var sort: QuerySort? = null

    init {
        // TODO: generate a real id
        id = (Objects.hash(filter) + Objects.hash(sort)).toString()
        channelCIDs = emptyList()
    }


    /** we track when the query was created and updated so we can clear out old results */
    var createdAt: Date? = null
    var updatedAt: Date? = null
}