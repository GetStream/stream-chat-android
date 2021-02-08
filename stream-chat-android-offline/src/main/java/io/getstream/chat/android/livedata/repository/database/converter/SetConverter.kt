package io.getstream.chat.android.livedata.repository.database.converter

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import io.getstream.chat.android.livedata.gson

internal class SetConverter {

    @TypeConverter
    fun stringToSortedSet(data: String?): MutableSet<String> {
        if (data.isNullOrEmpty() || data == "null") {
            return mutableSetOf()
        }
        val sortedSetType = object :
            TypeToken<MutableSet<String>>() {}.type
        return gson.fromJson(
            data,
            sortedSetType
        )
    }

    @TypeConverter
    fun sortedSetToString(someObjects: MutableSet<String>?): String {
        return gson.toJson(someObjects)
    }
}
