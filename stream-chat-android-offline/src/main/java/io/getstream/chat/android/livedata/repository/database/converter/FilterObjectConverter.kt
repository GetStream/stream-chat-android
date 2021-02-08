package io.getstream.chat.android.livedata.repository.database.converter

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.livedata.gson
import java.lang.reflect.Type

internal class FilterObjectConverter {
    @TypeConverter
    fun stringToObject(data: String?): FilterObject {
        if (data.isNullOrEmpty() || data == "null") {
            return FilterObject()
        }
        val hashType: Type = object : TypeToken<HashMap<String, Any>?>() {}.type
        val dataMap: HashMap<String, Any> = gson.fromJson(data, hashType)
        return FilterObject(dataMap)
    }

    @TypeConverter
    fun objectToString(someObjects: FilterObject?): String {
        if (someObjects == null) return ""

        return gson.toJson(someObjects.toMap())
    }
}
