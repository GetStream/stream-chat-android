package io.getstream.chat.android.offline.repository.database.converter

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import io.getstream.chat.android.offline.gson

internal class ExtraDataConverter {
    @TypeConverter
    fun stringToMap(data: String?): MutableMap<String, Any> {
        if (data.isNullOrEmpty() || data == "null") {
            return mutableMapOf()
        }
        val mapType = object :
            TypeToken<MutableMap<String?, Any?>?>() {}.type
        return gson.fromJson(
            data,
            mapType
        )
    }

    @TypeConverter
    fun mapToString(someObjects: MutableMap<String, Any>?): String {
        if (someObjects == null) {
            return "{}"
        }
        return gson.toJson(someObjects)
    }
}
