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
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.User
import java.util.Date

internal class MemberAdapter(
    private val userAdapter: JsonAdapter<User>,
    private val dateAdapter: JsonAdapter<Date>,
) : JsonAdapter<Member>() {

    @Suppress("LongMethod")
    override fun fromJson(reader: JsonReader): Member? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()

        reader.beginObject()

        var user: User? = null
        var createdAt: Date? = null
        var updatedAt: Date? = null
        var invited: Boolean? = null
        var inviteAcceptedAt: Date? = null
        var inviteRejectedAt: Date? = null
        var shadowBanned: Boolean? = null
        var banned: Boolean? = null
        var channelRole: String? = null
        var notificationsMuted: Boolean? = null
        var status: String? = null
        var banExpires: Date? = null
        var pinnedAt: Date? = null
        var archivedAt: Date? = null
        var extraData: MutableMap<String, Any>? = null

        while (reader.hasNext()) {
            val key = reader.nextName()
            when (key) {
                "user" -> user = userAdapter.fromJson(reader)
                "created_at" -> createdAt = dateAdapter.fromJson(reader)
                "updated_at" -> updatedAt = dateAdapter.fromJson(reader)
                "invited" -> invited = reader.nextBoolean()
                "invite_accepted_at" -> inviteAcceptedAt = dateAdapter.fromJson(reader)
                "invite_rejected_at" -> inviteRejectedAt = dateAdapter.fromJson(reader)
                "shadow_banned" -> shadowBanned = reader.nextBoolean()
                "banned" -> banned = reader.nextBoolean()
                "channel_role" -> channelRole = reader.nextString()
                "notifications_muted" -> notificationsMuted = reader.nextBoolean()
                "status" -> status = reader.nextString()
                "ban_expires" -> banExpires = dateAdapter.fromJson(reader)
                "pinned_at" -> pinnedAt = dateAdapter.fromJson(reader)
                "archived_at" -> archivedAt = dateAdapter.fromJson(reader)
                else -> reader.readJsonValue()?.let { value ->
                    val map = extraData ?: mutableMapOf<String, Any>().also { extraData = it }
                    map[key] = value
                }
            }
        }
        reader.endObject()

        if (user == null) {
            throw JsonDataException(
                "com.squareup.moshi.JsonDataException: " +
                    "Required value 'user' missing at ${reader.path} at ${reader.path}",
            )
        }

        return Member(
            user = user,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isInvited = invited,
            inviteAcceptedAt = inviteAcceptedAt,
            inviteRejectedAt = inviteRejectedAt,
            shadowBanned = shadowBanned ?: false,
            banned = banned ?: false,
            channelRole = channelRole,
            notificationsMuted = notificationsMuted,
            status = status,
            banExpires = banExpires,
            pinnedAt = pinnedAt,
            archivedAt = archivedAt,
            extraData = extraData?.toMap() ?: emptyMap(),
        )
    }

    override fun toJson(p0: JsonWriter, p1: Member?) {
        error("Serialization not supported for direct-to-domain path")
    }
}
