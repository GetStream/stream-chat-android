package io.getstream.chat.android.livedata.converter

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.livedata.gson

object QuerySortConverter {
    @TypeConverter
    @JvmStatic
    fun stringToObject(data: String?): QuerySort? {
        if (data == null || data.isEmpty()) {
            return null
        }
        val listType = object : TypeToken<QuerySort>() {}.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    @JvmStatic
    fun objectToString(someObjects: QuerySort?): String {
        if (someObjects== null) return ""

        return gson.toJson(someObjects)
    }
}
