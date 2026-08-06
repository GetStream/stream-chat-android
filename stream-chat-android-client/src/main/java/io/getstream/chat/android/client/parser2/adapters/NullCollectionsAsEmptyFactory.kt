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
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.rawType
import java.lang.reflect.Type
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor

/**
 * Reads an explicit `null` as an empty collection for the non-null [List] and [Map] properties of
 * the generated network models.
 *
 * The models are generated from the v2 spec, whose encoder writes nil Go maps and slices as `{}`
 * and `[]`, so those properties are non-null. We call the v1 endpoints, whose encoder writes them
 * as `null` instead (e.g. `thread_participants[].custom`), which would otherwise fail to parse.
 *
 * Remove once the client calls the v2 endpoints, where the shape already matches the models.
 */
internal object NullCollectionsAsEmptyFactory : JsonAdapter.Factory {

    private const val GENERATED_MODELS_PACKAGE = "io.getstream.chat.android.network.models"

    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
        if (annotations.isNotEmpty()) return null
        val rawType = type.rawType
        if (rawType.`package`?.name != GENERATED_MODELS_PACKAGE) return null

        val emptyValues = emptyValuesByJsonName(rawType)
        if (emptyValues.isEmpty()) return null

        return NullCollectionsAsEmptyAdapter(
            delegate = moshi.nextAdapter(this, type, annotations),
            mapAdapter = moshi.adapter(
                Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java),
            ),
            emptyValues = emptyValues,
        )
    }

    /**
     * Wire names of the non-null collection properties of [rawType], mapped to their empty value.
     *
     * Relies on the Kotlin metadata of the generated models, which `consumer-proguard-rules.pro`
     * keeps. Failures are deliberately not caught: swallowing them would silently stop coercing
     * nulls and turn this into a release-only parsing crash.
     */
    private fun emptyValuesByJsonName(rawType: Class<*>): Map<String, Any> =
        rawType.kotlin.primaryConstructor?.parameters
            .orEmpty()
            .mapNotNull { parameter ->
                if (parameter.type.isMarkedNullable) return@mapNotNull null
                val empty: Any = when (parameter.type.classifier) {
                    List::class -> emptyList<Any?>()
                    Map::class -> emptyMap<String, Any?>()
                    else -> return@mapNotNull null
                }
                val name = parameter.findAnnotation<Json>()?.name ?: parameter.name ?: return@mapNotNull null
                name to empty
            }
            .toMap()
}

private class NullCollectionsAsEmptyAdapter(
    private val delegate: JsonAdapter<Any>,
    private val mapAdapter: JsonAdapter<MutableMap<String, Any?>>,
    private val emptyValues: Map<String, Any>,
) : JsonAdapter<Any>() {

    override fun fromJson(reader: JsonReader): Any? {
        if (reader.peek() == JsonReader.Token.NULL) {
            return reader.nextNull()
        }
        val map = mapAdapter.fromJson(reader) ?: return null
        emptyValues.forEach { (name, empty) ->
            if (map.containsKey(name) && map[name] == null) {
                map[name] = empty
            }
        }
        return delegate.fromJsonValue(map)
    }

    override fun toJson(writer: JsonWriter, value: Any?) = delegate.toJson(writer, value)
}
