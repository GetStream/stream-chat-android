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
import io.getstream.chat.android.models.UserGroup
import java.util.Date

internal class UserGroupAdapter(
    private val dateAdapter: JsonAdapter<Date>,
) : JsonAdapter<UserGroup>() {

    override fun fromJson(reader: JsonReader): UserGroup? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()

        reader.beginObject()

        var id: String? = null
        var name: String? = null
        var description: String? = null
        var teamId: String? = null
        var createdBy: String? = null
        var createdAt: Date? = null
        var updatedAt: Date? = null

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "id" -> id = reader.nextString()
                "name" -> name = reader.nextString()
                "description" -> description = JsonParsingUtils.readNullableString(reader)
                "team_id" -> teamId = JsonParsingUtils.readNullableString(reader)
                "created_by" -> createdBy = JsonParsingUtils.readNullableString(reader)
                "created_at" -> createdAt = dateAdapter.fromJson(reader)
                "updated_at" -> updatedAt = dateAdapter.fromJson(reader)
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        JsonParsingUtils.requireField(id, "id", reader)
        JsonParsingUtils.requireField(name, "name", reader)

        return UserGroup(
            id = id,
            name = name,
            description = description,
            team = teamId.orEmpty(),
            members = emptyList(),
            createdBy = createdBy,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    override fun toJson(p0: JsonWriter, p1: UserGroup?) {
        error("Serialization not supported for direct-to-domain path")
    }
}
