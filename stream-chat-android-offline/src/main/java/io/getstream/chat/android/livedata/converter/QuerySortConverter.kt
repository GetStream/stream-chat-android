package io.getstream.chat.android.livedata.converter

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.livedata.gson

/**
 * QuerySort can be null so we need to handle that here
 */
internal class QuerySortConverter {
    @TypeConverter
    fun stringToObject(data: String?): QuerySort? {
        if (data.isNullOrEmpty() || data == "null") {
            return null
        }
        val listType = object : TypeToken<QuerySort>() {}.type
        val sort: QuerySort = gson.fromJson(data, listType)
        val newData: MutableList<Map<String, Any>> = mutableListOf()
        for (map in sort.data) {
            // cast floats to ints
            val newMap = mutableMapOf<String, Any>()
            newMap["direction"] = (map["direction"] as Double).toInt()
            newMap["field"] = map["field"] as String
            newData.add(newMap)
        }
        return QuerySort(newData)
    }

    @TypeConverter
    fun objectToString(someObjects: QuerySort?): String {
        if (someObjects == null) return ""

        return gson.toJson(someObjects)
    }
}
