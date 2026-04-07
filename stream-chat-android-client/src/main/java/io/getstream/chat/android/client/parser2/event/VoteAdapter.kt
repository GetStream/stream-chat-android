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

package io.getstream.chat.android.client.parser2.event

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.Vote
import java.util.Date

internal class VoteAdapter(
    private val userAdapter: JsonAdapter<User>,
    private val dateAdapter: JsonAdapter<Date>,
) : JsonAdapter<Vote>() {
    override fun fromJson(reader: JsonReader): Vote? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()

        reader.beginObject()

        var id: String? = null
        var pollId: String? = null
        var optionId: String? = null
        var createdAt: Date? = null
        var updatedAt: Date? = null
        var user: User? = null

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "id" -> id = reader.nextString()
                "poll_id" -> pollId = reader.nextString()
                "option_id" -> optionId = reader.nextString()
                "created_at" -> createdAt = dateAdapter.fromJson(reader)
                "updated_at" -> updatedAt = dateAdapter.fromJson(reader)
                "user" -> user = userAdapter.fromJson(reader)
                "user_id" -> reader.skipValue()
                "is_answer" -> reader.skipValue()
                "answer_text" -> reader.skipValue()
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        if (id == null) {
            throw JsonDataException("Required value 'id' missing at ${reader.path}")
        }
        if (pollId == null) {
            throw JsonDataException("Required value 'poll_id' missing at ${reader.path}")
        }
        if (optionId == null) {
            throw JsonDataException("Required value 'option_id' missing at ${reader.path}")
        }
        if (createdAt == null) {
            throw JsonDataException("Required value 'created_at' missing at ${reader.path}")
        }
        if (updatedAt == null) {
            throw JsonDataException("Required value 'updated_at' missing at ${reader.path}")
        }

        return Vote(
            id = id,
            pollId = pollId,
            optionId = optionId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            user = user,
        )
    }

    override fun toJson(p0: JsonWriter, p1: Vote?) {
        error("Serialization not supported for direct-to-domain path")
    }
}
