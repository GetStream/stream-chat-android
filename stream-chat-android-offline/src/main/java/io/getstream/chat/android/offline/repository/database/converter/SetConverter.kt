package io.getstream.chat.android.offline.repository.database.converter

import androidx.room.TypeConverter
import com.squareup.moshi.adapter

internal class SetConverter {
    @OptIn(ExperimentalStdlibApi::class)
    private val adapter = moshi.adapter<MutableSet<String>>()

    @TypeConverter
    fun stringToSortedSet(data: String?): MutableSet<String>? {
        if (data.isNullOrEmpty() || data == "null") {
            return mutableSetOf()
        }
        return adapter.fromJson(data)
    }

    @TypeConverter
    fun sortedSetToString(someObjects: MutableSet<String>?): String {
        return adapter.toJson(someObjects)
    }
}
