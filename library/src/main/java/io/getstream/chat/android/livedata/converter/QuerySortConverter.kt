package io.getstream.chat.android.livedata.converter

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.livedata.gson

/**
 * QuerySort can be null so we need to handle that here
 */
class QuerySortConverter {
    @TypeConverter
    fun stringToObject(data: String?): QuerySort? {
        if (data.isNullOrEmpty() || data == "null") {
            return null
        }
        val listType = object : TypeToken<QuerySort>() {}.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun objectToString(someObjects: QuerySort?): String {
        if (someObjects == null) return ""

        return gson.toJson(someObjects)
    }
}
