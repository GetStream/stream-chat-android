package io.getstream.chat.android.livedata.converter

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.api.models.QuerySort.Companion.asc
import io.getstream.chat.android.client.api.models.QuerySort.Companion.desc
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.livedata.gson

internal class ChannelsQuerySortConverter {
    @TypeConverter
    fun stringToObject(data: String?): QuerySort<Channel>? {
        if (data.isNullOrEmpty() || data == "null") {
            return null
        }
        val listType = object : TypeToken<List<Map<String, String>>>() {}.type
        val fieldSpecs: List<Map<String, String>> = gson.fromJson(data, listType)
        val result = QuerySort<Channel>()
        for (map in fieldSpecs) {
            // cast floats to ints
            val fieldName = map[KEY_FIELD_NAME] ?: error("Serialization error of QuerySort")
            val direction = map[KEY_SORT_DIRECTION]?.toInt() ?: error("Serialization error of QuerySort")
            when (direction) {
                QuerySort.SortDirection.ASC.value -> result.asc(fieldName)
                QuerySort.SortDirection.DESC.value -> result.desc(fieldName)
                else -> error("Direction must be ASC/DESC")
            }
        }
        return result
    }

    @TypeConverter
    fun objectToString(someObjects: QuerySort<Channel>?): String {
        if (someObjects == null) return ""

        return gson.toJson(someObjects.getListOfSpecification())
    }

    private fun QuerySort<*>.getListOfSpecification(): List<Map<String, String>> = sortSpecifications.map {
        mapOf(
            KEY_FIELD_NAME to it.field.name,
            KEY_SORT_DIRECTION to it.sortDirection.value.toString()
        )
    }

    private companion object {
        private const val KEY_FIELD_NAME = "KEY_FIELD_NAME"
        private const val KEY_SORT_DIRECTION = "KEY_SORT_DIRECTION"
    }
}
