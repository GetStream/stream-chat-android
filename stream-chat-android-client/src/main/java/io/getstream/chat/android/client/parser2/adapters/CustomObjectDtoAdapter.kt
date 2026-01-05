/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.parser2.adapters

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import kotlin.reflect.KClass

/**
 * Base class for implementing Moshi adapters that support our API's dynamic
 * JSON models.
 */
internal open class CustomObjectDtoAdapter<Value : Any>(private val kClass: KClass<Value>) {

    private companion object {
        private const val EXTRA_DATA = "extraData"
    }

    /**
     * Names of the declared properties that are inside the [Value] type being
     * handled. These will not be copied into extraData when parsing with
     * [parseWithExtraData].
     */
    private val memberNames: List<String> by lazy {
        kClass.members.map { member -> member.name }.minus(EXTRA_DATA)
    }

    /**
     * Moves all values in the input JSON that are not declared properties of
     * [Value] into an extraValue field, and then parses a [Value] instance
     * from this transformed data.
     */
    protected fun parseWithExtraData(
        jsonReader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        valueAdapter: JsonAdapter<Value>,
    ): Value? {
        if (jsonReader.peek() == JsonReader.Token.NULL) {
            jsonReader.nextNull<Nothing?>()
            return null
        }

        // Parse full JSON content as a MutableMap
        val map = mapAdapter.fromJson(jsonReader)!!

        val extraData = mutableMapOf<String, Any>()

        // Save the value of the literal "extraData" field at the root of the object, if present
        map[EXTRA_DATA]?.let { explicitExtraData ->
            extraData[EXTRA_DATA] = explicitExtraData
        }

        // Save the values of non-member fields as extra data
        map.forEach { entry ->
            if (entry.key !in memberNames) {
                extraData[entry.key] = entry.value
            }
        }

        // Replace original "extraData" with the newly collected values
        map[EXTRA_DATA] = extraData

        // Parse output value object from the transformed Map
        return valueAdapter.fromJsonValue(map)!!
    }

    /**
     * Converts the input [value] into a Map, moves whatever it contained in its
     * extraData property to top level values inside the Map, and writes this
     * transformed Map into [jsonWriter].
     */
    @Suppress("UNCHECKED_CAST")
    protected fun serializeWithExtraData(
        jsonWriter: JsonWriter,
        value: Value?,
        mapAdapter: JsonAdapter<MutableMap<String, Any?>>,
        valueAdapter: JsonAdapter<Value>,
    ) {
        if (value == null) {
            jsonWriter.nullValue()
            return
        }

        // Convert input value into a Map
        val map: MutableMap<String, Any?> = valueAdapter.toJsonValue(value) as MutableMap<String, Any?>

        // Grab real "extraData" property's value
        val extraData = map[EXTRA_DATA] as Map<String, Any?>

        // Remove literal "extraData" field from Map
        map.remove(EXTRA_DATA)

        // Merge all values from "extraData" property back into the Map as top level fields
        map.putAll(extraData)

        // Write Map to output
        mapAdapter.toJson(jsonWriter, map)
    }
}
