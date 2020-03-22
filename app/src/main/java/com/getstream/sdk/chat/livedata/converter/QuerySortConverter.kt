package com.getstream.sdk.chat.livedata.converter

import androidx.room.TypeConverter
import com.getstream.sdk.chat.livedata.gson
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.getstream.chat.android.client.api.models.QuerySort

object QuerySortConverter {
    @TypeConverter
    @JvmStatic
    fun stringToObject(data: String?): QuerySort {
        if (data == null) {
            return QuerySort()
        }
        val listType = object : TypeToken<QuerySort?>() {}.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    @JvmStatic
    fun objectToString(someObjects: QuerySort?): String {
        return gson.toJson(someObjects)
    }
}
