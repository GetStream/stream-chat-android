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
import io.getstream.chat.android.models.UserGroupMember
import java.util.Date

internal class UserGroupMemberAdapter(
    private val dateAdapter: JsonAdapter<Date>,
) : JsonAdapter<UserGroupMember>() {

    override fun fromJson(reader: JsonReader): UserGroupMember? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()

        reader.beginObject()

        var groupId: String? = null
        var userId: String? = null
        var isAdmin: Boolean? = null
        var createdAt: Date? = null

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "group_id" -> groupId = reader.nextString()
                "user_id" -> userId = reader.nextString()
                "is_admin" -> isAdmin = reader.nextBoolean()
                "created_at" -> createdAt = dateAdapter.fromJson(reader)
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        JsonParsingUtils.requireField(groupId, "group_id", reader)
        JsonParsingUtils.requireField(userId, "user_id", reader)

        return UserGroupMember(
            groupId = groupId,
            userId = userId,
            isAdmin = isAdmin ?: false,
            createdAt = createdAt,
        )
    }

    override fun toJson(p0: JsonWriter, p1: UserGroupMember?) {
        error("Serialization not supported for direct-to-domain path")
    }
}
