package io.getstream.chat.android.client.offline.repository.database.converter

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import io.getstream.chat.android.client.parser.StreamGson

internal class SetConverter {

    @TypeConverter
    fun stringToSortedSet(data: String?): MutableSet<String> {
        if (data.isNullOrEmpty() || data == "null") {
            return mutableSetOf()
        }
        val sortedSetType = object :
            TypeToken<MutableSet<String>>() {}.type
        return StreamGson.gson.fromJson(
            data,
            sortedSetType
        )
    }

    @TypeConverter
    fun sortedSetToString(someObjects: MutableSet<String>?): String {
        return StreamGson.gson.toJson(someObjects)
    }
}
