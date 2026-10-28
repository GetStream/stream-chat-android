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
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.UserTransformer
import java.util.Date

/**
 * Reads a user straight into its domain model: custom data under `custom`, mapped by [mapping] as the generated path
 * maps it, with [hiddenKeys] the declared fields of the kind of user read.
 */
internal class UserAdapter(
    private val dateAdapter: JsonAdapter<Date>,
    private val userTransformer: UserTransformer,
    private val mapping: DomainMapping,
    private val hiddenKeys: Set<String>,
) : JsonAdapter<User>() {

    override fun fromJson(reader: JsonReader): User? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()
        return read(reader)
    }

    /** Matches keys with [KEYS] so no key string is allocated, as the generated adapters do. */
    @Suppress("LongMethod", "CyclomaticComplexMethod", "MagicNumber")
    private fun read(reader: JsonReader): User {
        reader.beginObject()
        var id: String? = null
        var name: String? = null
        var image: String? = null
        var role: String? = null
        var language: String? = null
        var banned: Boolean? = null
        var online: Boolean? = null
        var createdAt: Date? = null
        var deactivatedAt: Date? = null
        var updatedAt: Date? = null
        var lastActive: Date? = null
        var teams: List<String>? = null
        var teamsRole: Map<String, String>? = null
        var blockedUserIds: List<String>? = null
        var avgResponseTime: Long? = null
        var customData: Map<String, Any?>? = null
        var deletedAt: Date? = null
        var revokeTokensIssuedBefore: Date? = null

        while (reader.hasNext()) {
            when (reader.selectName(KEYS)) {
                0 -> avgResponseTime = JsonParsingUtils.readNullableInt(reader)?.toLong()
                1 -> banned = reader.nextBoolean()
                2 -> if (JsonParsingUtils.expectToken(reader, JsonReader.Token.BEGIN_ARRAY, nullable = false)) {
                    blockedUserIds = JsonParsingUtils.parseStringList(reader)
                }
                3 -> createdAt = dateAdapter.fromJson(reader)
                4 -> deactivatedAt = dateAdapter.fromJson(reader)
                5 -> id = reader.nextString()
                6 -> image = JsonParsingUtils.readNullableString(reader)
                7 -> language = JsonParsingUtils.readNullableString(reader)
                8 -> lastActive = dateAdapter.fromJson(reader)
                9 -> name = JsonParsingUtils.readNullableString(reader)
                10 -> online = reader.nextBoolean()
                11 -> role = reader.nextString()
                12 -> if (JsonParsingUtils.expectToken(reader, JsonReader.Token.BEGIN_ARRAY, nullable = false)) {
                    teams = JsonParsingUtils.parseStringList(reader)
                }
                13 -> if (JsonParsingUtils.expectToken(reader, JsonReader.Token.BEGIN_OBJECT, nullable = true)) {
                    teamsRole = JsonParsingUtils.parseStringMap(reader)
                }
                14 -> updatedAt = dateAdapter.fromJson(reader)
                15 -> if (JsonParsingUtils.expectToken(reader, JsonReader.Token.BEGIN_OBJECT, nullable = false)) {
                    customData = JsonParsingUtils.readJsonObject(reader)
                }
                16 -> deletedAt = dateAdapter.fromJson(reader)
                17 -> revokeTokensIssuedBefore = dateAdapter.fromJson(reader)
                else -> {
                    reader.skipName()
                    reader.skipValue()
                }
            }
        }
        reader.endObject()

        JsonParsingUtils.requireField(id, "id", reader)
        JsonParsingUtils.requireField(role, "role", reader)
        JsonParsingUtils.requireField(banned, "banned", reader)
        JsonParsingUtils.requireField(online, "online", reader)
        // Required by the generated v2 model, so the direct path fails where the generated one does.
        JsonParsingUtils.requireField(createdAt, "createdAt", reader)
        JsonParsingUtils.requireField(updatedAt, "updatedAt", reader)
        JsonParsingUtils.requireField(language, "language", reader)

        val stored = customData.orEmpty()
        return User(
            id = id,
            name = name.orEmpty(),
            image = image.orEmpty(),
            role = role,
            language = language,
            banned = banned,
            online = online,
            createdAt = createdAt,
            deactivatedAt = deactivatedAt,
            updatedAt = updatedAt,
            lastActive = lastActive,
            teams = teams.orEmpty(),
            teamsRole = teamsRole.orEmpty(),
            blockedUserIds = blockedUserIds.orEmpty(),
            avgResponseTime = avgResponseTime,
            extraData = mapping.userExtraData(stored, hiddenKeys, deletedAt, revokeTokensIssuedBefore),
        ).let(userTransformer::transform)
    }

    private companion object {
        private val KEYS = JsonReader.Options.of(
            "avg_response_time", "banned", "blocked_user_ids", "created_at", "deactivated_at", "id", "image",
            "language", "last_active", "name", "online", "role", "teams", "teams_role", "updated_at", "custom",
            "deleted_at", "revoke_tokens_issued_before",
        )
    }

    override fun toJson(p0: JsonWriter, p1: User?) {
        error("Serialization not supported for direct-to-domain path")
    }
}
