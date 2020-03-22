package com.getstream.sdk.chat.livedata.converter

import androidx.room.TypeConverter
import com.getstream.sdk.chat.livedata.gson
import com.google.gson.Gson
import io.getstream.chat.android.client.utils.FilterObject
import java.lang.reflect.Type
import com.google.gson.reflect.TypeToken


object FilterObjectConverter {
    @TypeConverter
    @JvmStatic
    fun stringToObject(data: String?): FilterObject {
        if (data == null) {
            return FilterObject()
        }
        val listType: Type = object : TypeToken<FilterObject?>() {}.getType()
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    @JvmStatic
    fun objectToString(someObjects: FilterObject?): String {
        return gson.toJson(someObjects)
    }
}


