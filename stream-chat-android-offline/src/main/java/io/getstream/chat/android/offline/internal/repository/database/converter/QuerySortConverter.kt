package io.getstream.chat.android.offline.internal.repository.database.converter

import androidx.room.TypeConverter
import com.squareup.moshi.adapter
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel

internal class QuerySortConverter {

    @OptIn(ExperimentalStdlibApi::class)
    private val adapter = moshi.adapter<List<Map<String, Any>>>()

    @TypeConverter
    fun stringToObject(data: String?): QuerySort<Channel> {
        if (data.isNullOrEmpty()) {
            return QuerySort()
        }
        val listOfSortSpec = adapter.fromJson(data)
        return listOfSortSpec?.let(::parseQuerySort) ?: QuerySort()
    }

    private fun parseQuerySort(listOfSortSpec: List<Map<String, Any>>): QuerySort<Channel> {
        return listOfSortSpec.fold(QuerySort()) { sort, sortSpecMap ->
            val fieldName = sortSpecMap[QuerySort.KEY_FIELD_NAME] as? String ?: error("Cannot parse sortSpec to query sort\n$sortSpecMap")
            val direction = (sortSpecMap[QuerySort.KEY_DIRECTION] as? Number)?.toInt() ?: error("Cannot parse sortSpec to query sort\n$sortSpecMap")
            when (direction) {
                QuerySort.SortDirection.ASC.value -> sort.asc(fieldName, Channel::class.java)
                QuerySort.SortDirection.DESC.value -> sort.desc(fieldName, Channel::class.java)
                else -> error("Cannot parse sortSpec to query sort\n$sortSpecMap")
            }
        }
    }

    @TypeConverter
    fun objectToString(querySort: QuerySort<Channel>): String {
        return adapter.toJson(querySort.toDto())
    }
}
