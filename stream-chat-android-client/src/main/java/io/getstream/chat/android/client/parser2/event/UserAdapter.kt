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
import io.getstream.chat.android.PrivacySettings
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.UserTransformer
import java.util.Date

internal class UserAdapter(
    private val deviceAdapter: JsonAdapter<Device>,
    private val privacySettingsAdapter: JsonAdapter<PrivacySettings>,
    private val dateAdapter: JsonAdapter<Date>,
    private val userTransformer: UserTransformer,
) : JsonAdapter<User>() {

    @Suppress("LongMethod")
    override fun fromJson(reader: JsonReader): User? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()

        reader.beginObject()

        var id: String? = null
        var name: String? = null
        var image: String? = null
        var role: String? = null
        var invisible: Boolean? = null
        var privacySettings: PrivacySettings? = null
        var language: String? = null
        var banned: Boolean? = null
        var devices: List<Device>? = null
        var online: Boolean? = null
        var createdAt: Date? = null
        var deactivatedAt: Date? = null
        var updatedAt: Date? = null
        var lastActive: Date? = null
        var teams: List<String>? = null
        var teamsRole: Map<String, String>? = null
        var blockedUserIds: List<String>? = null
        var avgResponseTime: Long? = null
        var extraData: MutableMap<String, Any>? = null

        while (reader.hasNext()) {
            val key = reader.nextName()
            when (key) {
                "avg_response_time" -> avgResponseTime = reader.nextLong()
                "banned" -> banned = reader.nextBoolean()
                "blocked_user_ids" -> blockedUserIds = JsonParsingUtils.parseStringList(reader)
                "created_at" -> createdAt = dateAdapter.fromJson(reader)
                "deactivated_at" -> deactivatedAt = dateAdapter.fromJson(reader)
                "devices" -> devices = JsonParsingUtils.parseList(reader, deviceAdapter)
                "id" -> id = reader.nextString()
                "image" -> image = reader.nextString()
                "invisible" -> invisible = reader.nextBoolean()
                "language" -> language = reader.nextString()
                "last_active" -> lastActive = dateAdapter.fromJson(reader)
                "name" -> name = reader.nextString()
                "online" -> online = reader.nextBoolean()
                "privacy_settings" -> privacySettings = privacySettingsAdapter.fromJson(reader)
                "role" -> role = reader.nextString()
                "teams" -> teams = JsonParsingUtils.parseStringList(reader)
                "teams_role" -> teamsRole = JsonParsingUtils.parseStringMap(reader)
                "updated_at" -> updatedAt = dateAdapter.fromJson(reader)

                // The following are not part of the UserResponse (they are part of OwnUserResponse):
                // This is an intentional change from DownstreamUserDto which covers both UserResponse/OwnUserResponse
                // 1. total_unread_count
                // 2. unread_channels
                // 3. unread_threads
                // 4. mutes
                // 5. channel_mutes
                // 6. push_preferences
                else -> reader.readJsonValue()?.let { value ->
                    val map = extraData ?: mutableMapOf<String, Any>().also { extraData = it }
                    map[key] = value
                }
            }
        }
        reader.endObject()

        if (id == null) {
            throw JsonDataException(
                "com.squareup.moshi.JsonDataException: " +
                    "Required value 'id' missing at ${reader.path} at ${reader.path}",
            )
        }
        if (role == null) {
            throw JsonDataException(
                "com.squareup.moshi.JsonDataException: " +
                    "Required value 'role' missing at ${reader.path} at ${reader.path}",
            )
        }
        if (banned == null) {
            throw JsonDataException(
                "com.squareup.moshi.JsonDataException: " +
                    "Required value 'banned' missing at ${reader.path} at ${reader.path}",
            )
        }
        if (online == null) {
            throw JsonDataException(
                "com.squareup.moshi.JsonDataException: " +
                    "Required value 'online' missing at ${reader.path} at ${reader.path}",
            )
        }

        return User(
            id = id,
            name = name ?: "",
            image = image ?: "",
            role = role,
            invisible = invisible ?: false,
            privacySettings = privacySettings,
            language = language ?: "",
            banned = banned,
            devices = devices ?: emptyList(),
            online = online,
            createdAt = createdAt,
            deactivatedAt = deactivatedAt,
            updatedAt = updatedAt,
            lastActive = lastActive,
            teams = teams ?: emptyList(),
            teamsRole = teamsRole ?: emptyMap(),
            blockedUserIds = blockedUserIds ?: emptyList(),
            avgResponseTime = avgResponseTime,
            extraData = extraData?.toMap() ?: emptyMap(),
        ).let(userTransformer::transform)
    }

    override fun toJson(p0: JsonWriter, p1: User?) {
        error("Serialization not supported for direct-to-domain path")
    }
}
