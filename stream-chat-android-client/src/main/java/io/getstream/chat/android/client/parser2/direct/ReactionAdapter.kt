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
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.User
import java.util.Date

internal class ReactionAdapter(
    private val userAdapter: JsonAdapter<User>,
    private val dateAdapter: JsonAdapter<Date>,
) : JsonAdapter<Reaction>() {

    @Suppress("LongMethod")
    override fun fromJson(reader: JsonReader): Reaction? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()

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

        return Reaction(
            messageId = messageId,
            type = type,
            score = score,
            userId = userId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            user = user,
            emojiCode = emojiCode,
            extraData = extraData?.toMap() ?: emptyMap(),
        )
    }

    override fun toJson(p0: JsonWriter, p1: Reaction?) {
        error("Serialization not supported for direct-to-domain path")
    }
}
