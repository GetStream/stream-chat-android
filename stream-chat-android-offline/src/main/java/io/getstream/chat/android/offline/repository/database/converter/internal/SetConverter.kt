package io.getstream.chat.android.offline.repository.database.converter.internal

import androidx.room.TypeConverter
import com.squareup.moshi.adapter

internal class SetConverter {
    @OptIn(ExperimentalStdlibApi::class)
    private val adapter = moshi.adapter<Set<String>>()

    @TypeConverter
    fun stringToSortedSet(data: String?): Set<String> {
        if (data.isNullOrEmpty() || data == "null") {
            return setOf()
        }
        return adapter.fromJson(data) ?: emptySet()
    }

    @TypeConverter
    fun sortedSetToString(someObjects: Set<String>?): String {
        return adapter.toJson(someObjects)
    }
}
