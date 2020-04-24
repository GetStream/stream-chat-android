package io.getstream.chat.android.livedata.converter

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import io.getstream.chat.android.livedata.gson
import java.util.*

class SetConverter {

    @TypeConverter
    fun stringToSortedSet(data: String?): SortedSet<String> {
        if (data.isNullOrEmpty() || data == "null") {
            return sortedSetOf()
        }
        val sortedSetType = object :
            TypeToken<SortedSet<String>>() {}.type
        return gson.fromJson(
            data,
            sortedSetType
        )
    }

    @TypeConverter
    fun sortedSetToString(someObjects: SortedSet<String>?): String {
        return gson.toJson(someObjects)
    }
}
