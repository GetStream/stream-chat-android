package com.getstream.sdk.chat.livedata.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


object ExtraDataConverter {
    var gson = Gson()
    @TypeConverter
    @JvmStatic

    fun stringToMap(data: String?): MutableMap<String, Any> {
        if (data == null) {
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
    @JvmStatic

    fun mapToString(someObjects: MutableMap<String?, Any?>?): String {
        return gson.toJson(someObjects)
    }
}
