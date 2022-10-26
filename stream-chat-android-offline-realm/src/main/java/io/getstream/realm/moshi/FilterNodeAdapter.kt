package io.getstream.realm.moshi

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import io.getstream.realm.entity.FilterNode
import io.getstream.realm.filter.KEY_AND
import io.getstream.realm.filter.KEY_IN
import io.getstream.realm.filter.KEY_NOR
import io.getstream.realm.filter.KEY_NOT_IN
import io.getstream.realm.filter.KEY_OR

internal class FilterNodeAdapter : JsonAdapter<FilterNode>() {

    override fun fromJson(reader: JsonReader): FilterNode {
        reader.beginObject()
        reader.skipName()

        val type = reader.nextString()

        reader.skipName()
        val node = when {
            (isCompositeNode(type)) -> readCompositeNode(reader, type)

            isMultipleValueNode(type) -> readMultipleNode(reader, type)

            else -> readSimpleNode(reader, type)
        }

        reader.endObject()
        return node
    }

    private fun readCompositeNode(reader: JsonReader, type: String): FilterNode {
        val nodeList: Set<FilterNode> = reader.readArray { this.fromJson(reader) }
        return FilterNode(filter_type = type, field = null, value = nodeList)
    }

    private fun readMultipleNode(reader: JsonReader, type: String): FilterNode {
        val field = reader.nextString()
        reader.skipName()
        val values = reader.readArray(reader::nextString)

        return FilterNode(filter_type = type, field = field, value = values)
    }

    private fun readSimpleNode(reader: JsonReader, type: String): FilterNode {
        val field = reader.nextString()
        reader.skipName()
        val value = reader.nextString()

        return FilterNode(filter_type = type, field = field, value = value)
    }

    override fun toJson(writer: JsonWriter, node: FilterNode?) {
        val type = node?.filter_type
        val field = node?.field
        val value = node?.value

        writer.beginObject()

        type?.let { nodeType ->
            writer.name("filter_type")
            writer.value(nodeType)
        }

        field?.let { nodeField ->
            writer.name("field")
            writer.value(nodeField)
        }



        value?.let { nodeValue ->
            writer.name("value")

            when {
                isCompositeNode(type) -> {
                    writer.beginArray()
                    (nodeValue as Iterable<FilterNode>).forEach { filterNode ->
                        toJson(writer, filterNode)
                    }
                    writer.endArray()
                }

                isMultipleValueNode(type) -> {
                    writer.beginArray()
                    (value as Set<String>).forEach(writer::value)
                    writer.endArray()
                }

                else -> {
                    writer.value(nodeValue as? String)
                }
            }
        }

        writer.endObject()
    }


    private fun <T> JsonReader.readArray(provider: () -> T): Set<T> {
        beginArray()

        val values = mutableSetOf<T>()
        while (hasNext()) {
            values.add(provider.invoke())
        }

        endArray()
        return values
    }

    private fun isCompositeNode(nodeType: String?): Boolean =
        nodeType == KEY_AND || nodeType == KEY_OR || nodeType == KEY_NOR

    private fun isMultipleValueNode(nodeType: String?): Boolean {
        return nodeType == KEY_IN || nodeType == KEY_NOT_IN
    }
}
