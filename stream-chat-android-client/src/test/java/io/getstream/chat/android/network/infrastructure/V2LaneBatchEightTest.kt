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

package io.getstream.chat.android.network.infrastructure

import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.api2.toSortParams
import io.getstream.chat.android.client.parser.toMap
import io.getstream.chat.android.models.BannedUsersSort
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.network.models.FileUploadResponse
import io.getstream.chat.android.network.models.MuteChannelRequest
import io.getstream.chat.android.network.models.QueryBannedUsersPayload
import io.getstream.chat.android.network.models.QueryBannedUsersResponse
import io.getstream.chat.android.network.models.Response
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Date

/**
 * Channel mutes, banned users and uploads parse their recorded v2 responses through the generated [Serializer] alone,
 * and send the same request bodies as on v1.
 */
internal class V2LaneBatchEightTest {

    private val mapping = DomainMapping(
        currentUserIdProvider = { "filip" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
        hidesStoredDeclaredCustom = true,
    )

    private inline fun <reified T> parse(json: String): T = Serializer.moshi.adapter(T::class.java).fromJson(json)!!

    private inline fun <reified T> v2Body(request: T): String = Serializer.moshi.adapter(T::class.java).toJson(request)

    @Test
    fun `A banned user parses its nested custom data and integer dates from v2`() {
        val ban = with(mapping) { parse<QueryBannedUsersResponse>(BANNED).bans.single().toDomain()!! }

        assertEquals("jaewoong", ban.user.id)
        assertEquals(mapOf("ban_probe" to "nested"), ban.user.extraData)
        assertEquals("filip", ban.bannedBy?.id)
        assertEquals("messaging:rp-filesize-1791793482385", ban.channel?.cid)
        assertEquals(Date(1791818741052), ban.createdAt)
        assertEquals(Date(1791818801052), ban.expires)
        assertEquals("v2cap", ban.reason)
    }

    @Test
    fun `A channel mute and an upload parse from v2`() {
        assertEquals("26.89ms", parse<Response>(UNMUTE).duration)
        assertEquals("https://cdn.example/probe.png", parse<FileUploadResponse>(UPLOAD).file)
    }

    @Test
    fun `A channel mute and a banned users query keep their v1 bodies`() {
        assertBody(
            """{"expiration":60000,"channel_cids":["messaging:general"]}""",
            MuteChannelRequest(channelCids = listOf("messaging:general"), expiration = 60000),
        )
        assertBody(
            """{"filter_conditions":{"${'$'}and":[{"channel_cid":"messaging:general"},""" +
                """{"created_at":{"${'$'}gt":"2026-10-12T15:25:41.052Z"}}]},"limit":2,"offset":1,""" +
                """"sort":[{"direction":-1,"field":"created_at"}]}""",
            QueryBannedUsersPayload(
                filterConditions = Filters.and(
                    Filters.eq("channel_cid", "messaging:general"),
                    Filters.greaterThan("created_at", Date(1791818741052)),
                ).toMap(),
                sort = QuerySortByField.descByName<BannedUsersSort>("created_at").toSortParams(),
                offset = 1,
                limit = 2,
            ),
        )
    }

    @Test
    fun `A banned users query sends its date filters as RFC 3339 on v2`() {
        val payload = QueryBannedUsersPayload(filterConditions = emptyMap(), createdAtAfter = Date(1791818741052))

        assertEquals("""{"filter_conditions":{},"created_at_after":"2026-10-12T15:25:41.052Z"}""", v2Body(payload))
    }

    private inline fun <reified T : Any> assertBody(expected: String, request: T) {
        assertEquals(expected, v2Body(request))
    }

    private companion object {
        private fun user(id: String, custom: String) = """{"id":"$id","custom":$custom,"language":"","role":"user",
            "teams":[],"created_at":1634853490403855000,"updated_at":1791810499684936000,"banned":false,
            "online":false,"blocked_user_ids":[]}"""

        /** The recorded channel also carries its config and creator, left out here. */
        private val BANNED = """{"bans":[{"channel":{"id":"rp-filesize-1791793482385","type":"messaging",
            "cid":"messaging:rp-filesize-1791793482385","created_at":1791793482623641000,
            "updated_at":1791793482623641000,"frozen":false,"disabled":false,"custom":{}},
            "user":${user("jaewoong", """{"ban_probe":"nested"}""")},"expires":1791818801052927000,
            "reason":"v2cap","banned_by":${user("filip", "{}")},"created_at":1791818741052927000}],
            "duration":"4.19ms"}"""

        private const val UNMUTE = """{"duration":"26.89ms"}"""

        /** The recorded file URL is a signed CDN link, shortened here. */
        private const val UPLOAD = """{"file":"https://cdn.example/probe.png","duration":"85.83ms"}"""
    }
}
