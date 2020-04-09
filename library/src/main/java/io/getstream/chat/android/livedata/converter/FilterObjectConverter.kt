package io.getstream.chat.android.livedata.converter

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.livedata.gson
import java.lang.reflect.Type


class FilterObjectConverter {
    @TypeConverter
    fun stringToObject(data: String?): FilterObject {
        if (data == null) {
            return FilterObject()
        }
        val listType: Type = object : TypeToken<FilterObject?>() {}.getType()
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun objectToString(someObjects: FilterObject?): String {
        return gson.toJson(someObjects)
    }
}


