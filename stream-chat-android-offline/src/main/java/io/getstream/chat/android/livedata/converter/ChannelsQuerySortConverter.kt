package io.getstream.chat.android.livedata.converter

import androidx.room.TypeConverter
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel

/**
 * QuerySort can be null so we need to handle that here
 */
internal class ChannelsQuerySortConverter {
    @TypeConverter
    fun stringToObject(data: String?): QuerySort<Channel>? {
        /*if (data.isNullOrEmpty() || data == "null") {
            return null
        }
        val listType = object : TypeToken<QuerySort<Channel>>() {}.type
        val sort: QuerySort<Channel> = gson.fromJson(data, listType)
        val newData: MutableList<Map<String, Any>> = mutableListOf()
        for (map in sort.data) {
            // cast floats to ints
            val newMap = mutableMapOf<String, Any>()
            newMap["direction"] = (map["direction"] as Double).toInt()
            newMap["field"] = map["field"] as String
            newData.add(newMap)
        }
        return QuerySort(newData)*/
        return QuerySort()
    }

    @TypeConverter
    fun objectToString(someObjects: QuerySort<Channel>?): String {
        /*if (someObjects == null) return ""

        return gson.toJson(someObjects)*/

        return ""
    }

    //private fun QuerySort<*>.getListOfSpecification(): List<List<Pair<String, Any>>> = sortSpecifications.map { listOf(it.) }
}
