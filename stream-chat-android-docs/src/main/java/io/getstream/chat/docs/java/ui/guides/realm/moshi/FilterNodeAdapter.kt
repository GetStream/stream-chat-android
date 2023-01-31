/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.docs.java.ui.guides.realm.moshi

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import io.getstream.chat.docs.java.ui.guides.realm.entities.FilterNode

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
        return FilterNode(filterType = type, field = null, value = nodeList)
    }

    private fun readMultipleNode(reader: JsonReader, type: String): FilterNode {
        val field = reader.nextString()
        reader.skipName()
        val values = reader.readArray(reader::nextString)

        return FilterNode(filterType = type, field = field, value = values)
    }

    private fun readSimpleNode(reader: JsonReader, type: String): FilterNode {
        val field = reader.nextString()
        var value: Any? = null
        if (reader.hasNext()) {
            reader.skipName()

            value = when (reader.peek()) {
                JsonReader.Token.STRING -> reader.nextString()
                JsonReader.Token.NUMBER -> reader.nextDouble()
                JsonReader.Token.BOOLEAN -> reader.nextBoolean()
                JsonReader.Token.NULL -> reader.nextNull()
                else -> null
            }
        }

        return FilterNode(filterType = type, field = field, value = value)
    }

    override fun toJson(writer: JsonWriter, node: FilterNode?) {
        val type = node?.filterType
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
                    writeCompositeNode(writer, nodeValue)
                }

                isMultipleValueNode(type) -> {
                    writeMultipleValueNode(writer, value)
                }

                else -> {
                    writeFilterNode(writer, nodeValue)
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

    private fun writeCompositeNode(writer: JsonWriter, nodeValue: Any) {
        writer.beginArray()
        (nodeValue as Iterable<FilterNode>).forEach { filterNode ->
            toJson(writer, filterNode)
        }
        writer.endArray()
    }

    private fun writeMultipleValueNode(writer: JsonWriter, value: Any) {
        writer.beginArray()
        (value as Iterable<String>).forEach(writer::value)
        writer.endArray()
    }

    private fun writeFilterNode(writer: JsonWriter, nodeValue: Any) {
        when (nodeValue) {
            is Boolean -> writer.value(nodeValue)
            is String -> writer.value(nodeValue)
            is Number -> writer.value(nodeValue)
            else -> throw IllegalStateException("Unsupported type of filter")
        }
    }
}
