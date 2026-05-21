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

package io.getstream.chat.android.client.parser2.direct

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import io.getstream.chat.android.models.ReactionGroup
import java.util.Date

/**
 * Parser for [ReactionGroup] objects.
 *
 * Intentionally NOT a [JsonAdapter]: a `ReactionGroup` cannot be parsed standalone because its
 * `type` field is not present in the JSON object — it comes from the surrounding map key when
 * parsing `reaction_groups: Map<String, ReactionGroup>`. Use [parseReactionGroupsMap] for the
 * common case, or [parseWithType] when the type is known externally.
 */
internal class ReactionGroupAdapter(
    private val dateAdapter: JsonAdapter<Date>,
) {

    /**
     * Parse a ReactionGroup with the given type.
     *
     * The type is not part of the JSON structure but is passed as a parameter,
     * typically from the map key when parsing `reaction_groups: Map<String, ReactionGroup>`.
     *
     * @param reader The JSON reader positioned at the start of a ReactionGroup object
     * @param type The reaction type (e.g., "like", "love") - comes from the map key
     * @return The parsed ReactionGroup, or null if the JSON value is null
     */
    fun parseWithType(reader: JsonReader, type: String): ReactionGroup? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()

        reader.beginObject()
        var count: Int? = null
        var sumScores: Int? = null
        var firstReactionAt: Date? = null
        var lastReactionAt: Date? = null

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "count" -> count = reader.nextInt()
                "sum_scores" -> sumScores = reader.nextInt()
                "first_reaction_at" -> firstReactionAt = dateAdapter.fromJson(reader)
                "last_reaction_at" -> lastReactionAt = dateAdapter.fromJson(reader)
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        JsonParsingUtils.requireField(count, "count", reader)
        JsonParsingUtils.requireField(sumScores, "sum_scores", reader)
        JsonParsingUtils.requireField(firstReactionAt, "first_reaction_at", reader)
        JsonParsingUtils.requireField(lastReactionAt, "last_reaction_at", reader)

        return ReactionGroup(
            type = type,
            count = count,
            sumScore = sumScores,
            firstReactionAt = firstReactionAt,
            lastReactionAt = lastReactionAt,
        )
    }

    /**
     * Helper method to parse a map of reaction groups from JSON.
     *
     * This is the typical use case: parsing `reaction_groups: Map<String, ReactionGroup>`.
     * The map key becomes the `type` parameter for each ReactionGroup.
     *
     * @param reader The JSON reader positioned at the start of an object containing reaction groups
     * @return A map of reaction type to ReactionGroup
     */
    fun parseReactionGroupsMap(reader: JsonReader): Map<String, ReactionGroup> {
        if (reader.peek() == JsonReader.Token.NULL) {
            reader.nextNull<Nothing?>()
            return emptyMap()
        }

        if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) {
            reader.skipValue()
            return emptyMap()
        }

        reader.beginObject()
        val groups = mutableMapOf<String, ReactionGroup>()
        while (reader.hasNext()) {
            val type = reader.nextName() // The map key is the reaction type
            parseWithType(reader, type)?.let {
                groups[type] = it
            }
        }
        reader.endObject()
        return groups
    }
}
