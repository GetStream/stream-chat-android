package io.getstream.realm.moshi

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import io.getstream.realm.entity.FilterNode

internal class FilterNodeAdapter : JsonAdapter<FilterNode>() {

    override fun fromJson(reader: JsonReader): FilterNode =
        FilterNode(
            filter_type = reader.nextString(),
            field = reader.nextString(),
            value = reader.nextString(),
        )

    override fun toJson(writer: JsonWriter, value: FilterNode?) {
        value?.filter_type?.let(writer::jsonValue)
        value?.field?.let(writer::jsonValue)
        value?.value?.let(writer::jsonValue)
    }
}
