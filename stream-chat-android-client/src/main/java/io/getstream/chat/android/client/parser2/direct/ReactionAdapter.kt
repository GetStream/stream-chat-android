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
import com.squareup.moshi.JsonWriter
import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.User
import java.util.Date

/**
 * Reads a reaction straight into its domain model. Without [v2Mapping] it reads the v1 shape, custom data at the root.
 * With it, the v2 shape: custom data under `custom`, mapped by [v2Mapping] as the generated v2 path maps it.
 */
internal class ReactionAdapter(
    private val userAdapter: JsonAdapter<User>,
    private val dateAdapter: JsonAdapter<Date>,
    private val v2Mapping: DomainMapping? = null,
) : JsonAdapter<Reaction>() {

    @Suppress("LongMethod")
    override fun fromJson(reader: JsonReader): Reaction? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()
        if (v2Mapping != null) return readV2(reader, v2Mapping)

        reader.beginObject()

        var messageId: String? = null
        var type: String? = null
        var score: Int? = null
        var userId: String? = null
        var createdAt: Date? = null
        var updatedAt: Date? = null
        var user: User? = null
        var emojiCode: String? = null
        var extraData: MutableMap<String, Any>? = null

        while (reader.hasNext()) {
            val key = reader.nextName()
            when (key) {
                "message_id" -> messageId = reader.nextString()
                "type" -> type = reader.nextString()
                "score" -> score = reader.nextInt()
                "user_id" -> userId = reader.nextString()
                "created_at" -> createdAt = dateAdapter.fromJson(reader)
                "updated_at" -> updatedAt = dateAdapter.fromJson(reader)
                "user" -> user = userAdapter.fromJson(reader)
                "emoji_code" -> emojiCode = JsonParsingUtils.readNullableString(reader)
                else -> extraData = JsonParsingUtils.accumulateExtraData(key, reader, extraData)
            }
        }
        reader.endObject()

        JsonParsingUtils.requireField(messageId, "message_id", reader)
        JsonParsingUtils.requireField(type, "type", reader)
        JsonParsingUtils.requireField(score, "score", reader)
        JsonParsingUtils.requireField(userId, "user_id", reader)
        JsonParsingUtils.requireField(createdAt, "created_at", reader)
        JsonParsingUtils.requireField(updatedAt, "updated_at", reader)
        JsonParsingUtils.requireField(user, "user", reader)

        return Reaction(
            messageId = messageId,
            type = type,
            score = score,
            userId = userId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            user = user,
            emojiCode = emojiCode,
            extraData = extraData?.toMap().orEmpty(),
        )
    }

    /** Reads the v2 shape, matching keys with [V2_KEYS] so no key string is allocated, as the generated adapters do. */
    @Suppress("MagicNumber")
    private fun readV2(reader: JsonReader, mapping: DomainMapping): Reaction {
        reader.beginObject()
        var messageId: String? = null
        var type: String? = null
        var score: Int? = null
        var userId: String? = null
        var createdAt: Date? = null
        var updatedAt: Date? = null
        var user: User? = null
        var custom: Map<String, Any?>? = null

        while (reader.hasNext()) {
            when (reader.selectName(V2_KEYS)) {
                0 -> messageId = reader.nextString()
                1 -> type = reader.nextString()
                2 -> score = reader.nextInt()
                3 -> userId = reader.nextString()
                4 -> createdAt = dateAdapter.fromJson(reader)
                5 -> updatedAt = dateAdapter.fromJson(reader)
                6 -> user = userAdapter.fromJson(reader)
                7 -> if (JsonParsingUtils.expectV2(reader, JsonReader.Token.BEGIN_OBJECT, nullable = false)) {
                    custom = JsonParsingUtils.readJsonObject(reader)
                }
                else -> {
                    reader.skipName()
                    reader.skipValue()
                }
            }
        }
        reader.endObject()

        JsonParsingUtils.requireField(messageId, "message_id", reader)
        JsonParsingUtils.requireField(type, "type", reader)
        JsonParsingUtils.requireField(score, "score", reader)
        JsonParsingUtils.requireField(userId, "user_id", reader)
        JsonParsingUtils.requireField(createdAt, "created_at", reader)
        JsonParsingUtils.requireField(updatedAt, "updated_at", reader)
        JsonParsingUtils.requireField(user, "user", reader)

        val stored = custom.orEmpty()
        return Reaction(
            messageId = messageId,
            type = type,
            score = score,
            userId = userId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            user = user,
            emojiCode = mapping.reactionEmojiCode(stored),
            extraData = mapping.reactionExtraData(stored),
        )
    }

    private companion object {
        private val V2_KEYS = JsonReader.Options.of(
            "message_id",
            "type",
            "score",
            "user_id",
            "created_at",
            "updated_at",
            "user",
            "custom",
        )
    }

    override fun toJson(p0: JsonWriter, p1: Reaction?) {
        error("Serialization not supported for direct-to-domain path")
    }
}
