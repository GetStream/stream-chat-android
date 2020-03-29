package io.getstream.chat.android.livedata.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.utils.FilterObject
import java.util.*

@Entity(tableName = "stream_channel_query")
data class QueryChannelsEntity(val filter: FilterObject) {

    constructor(filter: FilterObject, sort: QuerySort): this(filter) {
        this.sort = sort
    }

    @PrimaryKey
    var id: String

    var channelCIDs: MutableList<String> = mutableListOf()


    var sort: QuerySort? = null

    init {
        id = (Objects.hash(filter) + Objects.hash(sort)).toString()
    }

    /** we track when the query was created and updated so we can clear out old results */
    var createdAt: Date? = null
    var updatedAt: Date? = null
}