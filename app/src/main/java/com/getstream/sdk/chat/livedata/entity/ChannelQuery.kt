package com.getstream.sdk.chat.livedata.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.utils.FilterObject
import java.util.*

@Entity(tableName = "stream_channel_query")
data class ChannelQuery(val filter: FilterObject) {

    @PrimaryKey
    var id: String

    var sort: QuerySort? = null

    init {
        // TODO: generate a real id
        id = (Objects.hash(filter) + Objects.hash(sort)).toString()
    }


}