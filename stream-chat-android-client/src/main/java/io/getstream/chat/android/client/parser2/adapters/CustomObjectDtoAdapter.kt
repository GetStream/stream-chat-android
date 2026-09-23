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

import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor

/**
 * Base class for implementing Moshi adapters that support our API's dynamic
 * JSON models.
 *
 * [extraDataPropertyName] names the property that holds the overflow map (`extraData` or `custom`).
 *
 * [alsoKeepInExtraData] names declared properties that are additionally copied into the overflow map, with their raw
 * wire values. Useful when a key has to stay available in the overflow map as well as on its declared property.
 */
internal open class CustomObjectDtoAdapter<Value : Any>(
    private val kClass: KClass<Value>,
    private val extraDataPropertyName: String = "extraData",
    private val alsoKeepInExtraData: Set<String> = emptySet(),
    private val mergesNestedExtraData: Boolean = false,
) {

    /**
     * Wire-format names of the declared properties on [Value]. These will not be copied into the
     * overflow map when parsing with [parseWithExtraData]. Reads `@Json(name = ...)` first so
     * camelCase properties map to their snake_case wire names, falling back to the Kotlin
     * parameter name when `@Json` is absent.
     */
    private val memberNames: List<String> by lazy {
        kClass.primaryConstructor?.parameters.orEmpty().mapNotNull { param ->
            (param.findAnnotation<Json>()?.name ?: param.name)?.takeIf { it != extraDataPropertyName }
        }
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

        // Save the value of the literal extraData field at the root of the object, if present. When the
        // overflow property is also a real wire field, as the generated `custom` is, its contents are
        // merged instead, so a payload that nests the keys and one that inlines them at the root produce
        // the same map rather than one of them landing a level deeper.
        val mergedNested = collectExplicitExtraData(map[extraDataPropertyName], extraData)

        // Save the values of non-member fields as extra data. The overflow property is not a member name,
        // so it would otherwise be copied back in whole and undo the merge above.
        map.forEach { entry ->
            val alreadyMerged = mergedNested && entry.key == extraDataPropertyName
            if (!alreadyMerged && (entry.key !in memberNames || entry.key in alsoKeepInExtraData)) {
                extraData[entry.key] = entry.value
            }
        }

        // Replace original extraData with the newly collected values
        map[extraDataPropertyName] = extraData

        // Parse output value object from the transformed Map
        return valueAdapter.fromJsonValue(map)!!
    }

    /**
     * Collects the value already sitting under the overflow property name, returning whether it was
     * merged. A model whose overflow property is also a real wire field, as the generated `custom` is,
     * has its contents merged so that nesting and root-inlining produce the same map; anything else is
     * kept whole under its own name.
     */
    private fun collectExplicitExtraData(explicit: Any?, into: MutableMap<String, Any>): Boolean {
        if (!mergesNestedExtraData || explicit !is Map<*, *>) {
            if (explicit != null) into[extraDataPropertyName] = explicit
            return false
        }
        explicit.forEach { (key, value) ->
            if (key is String && value != null) into[key] = value
        }
        return true
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

        // Grab real extraData property's value
        val extraData = map[extraDataPropertyName] as? Map<String, Any?>

        // Remove literal extraData field from Map
        map.remove(extraDataPropertyName)

        // Merge all values from the extraData property back into the Map as top level fields
        extraData?.let(map::putAll)

        // Write Map to output
        mapAdapter.toJson(jsonWriter, map)
    }
}
