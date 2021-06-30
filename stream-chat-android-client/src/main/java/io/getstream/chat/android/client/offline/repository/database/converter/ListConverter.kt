package io.getstream.chat.android.client.offline.repository.database.converter

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import io.getstream.chat.android.client.parser.StreamGson

internal class ListConverter {
    @TypeConverter
    fun stringToStringList(data: String?): List<String>? {
        if (data.isNullOrEmpty() || data == "null") {
            return emptyList()
        }
        val listType = object :
            TypeToken<List<String?>?>() {}.type
        return StreamGson.gson.fromJson(
            data,
            listType
        )
    }

    @TypeConverter
    fun stringListToString(someObjects: List<String>?): String? {
        return StreamGson.gson.toJson(someObjects)
    }
}
