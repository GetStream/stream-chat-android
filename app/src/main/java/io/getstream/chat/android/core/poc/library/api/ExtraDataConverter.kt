package io.getstream.chat.android.core.poc.library.api

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.getstream.chat.android.core.poc.library.json.ChatGson
import java.lang.reflect.Type


object ExtraDataConverter {
    var gson = ChatGson.instance
    @TypeConverter
    fun stringToMap(data: String?): HashMap<String, Any> {
        if (data == null) {
            return HashMap()
        }
        val mapType: Type = object : TypeToken<HashMap<String?, Any?>?>() {}.type
        return gson.fromJson(data, mapType)
    }

    @TypeConverter
    fun mapToString(someObjects: HashMap<String?, Any?>?): String {
        return gson.toJson(someObjects)
    }
}
