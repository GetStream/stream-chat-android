package io.getstream.chat.android.offline.repository.database.converter

import androidx.room.TypeConverter
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel

internal object QuerySortConverter {

    @TypeConverter
    fun stringToObject(data: String): QuerySort<Channel> {
        return QuerySort()
    }

    @TypeConverter
    fun objectToString(querySort: QuerySort<Channel>): String {
        return ""
    }
}
