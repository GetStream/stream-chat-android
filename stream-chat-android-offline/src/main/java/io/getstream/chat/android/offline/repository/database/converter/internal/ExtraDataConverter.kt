package io.getstream.chat.android.offline.repository.database.converter.internal

import androidx.room.TypeConverter
import com.squareup.moshi.adapter

internal class ExtraDataConverter {
    @OptIn(ExperimentalStdlibApi::class)
    private val adapter = moshi.adapter<Map<String, Any>>()

    @TypeConverter
    fun stringToMap(data: String?): Map<String, Any>? {
        if (data.isNullOrEmpty() || data == "null") {
            return emptyMap()
        }
        return adapter.fromJson(data)
    }

    @TypeConverter
    fun mapToString(someObjects: Map<String, Any>?): String? {
        if (someObjects == null) {
            return "{}"
        }
        return adapter.toJson(someObjects)
    }
}
