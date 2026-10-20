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
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.network.models.ChannelMemberRequest
import io.getstream.chat.android.network.models.MembersResponse
import io.getstream.chat.android.network.models.QueryMembersPayload
import io.getstream.chat.android.network.models.SearchPayload
import io.getstream.chat.android.network.models.SearchResponse
import io.getstream.chat.android.network.models.SortParamRequest
import io.getstream.chat.android.network.models.WrappedUnreadCountsResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Date

/**
 * Search, members and unread counts parse their recorded v2 responses through the generated [Serializer] alone. Their
 * options travel as the JSON `payload` query parameter on both versions, with the same content except the custom data
 * of the members in a members query, which v2 keeps nested.
 */
internal class V2LaneBatchSixTest {

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
    fun `A search result parses its nested custom data and integer dates from v2`() {
        val message = with(mapping) { parse<SearchResponse>(SEARCH).results.single().message!!.toDomain() }

        assertEquals(mapOf("s_probe" to "nested"), message.extraData)
        assertEquals(Date(1791798765916), message.createdAt)
    }

    @Test
    fun `A member parses its user's nested custom data from v2`() {
        val member = with(mapping) { parse<MembersResponse>(MEMBERS).members.single().toDomain() }

        assertEquals(mapOf("birthland" to "Tatooine"), member.user.extraData)
        assertEquals(Date(1634854188381), member.createdAt)
    }

    @Test
    fun `Unread counts parse their integer dates from v2`() {
        val counts = with(mapping) { parse<WrappedUnreadCountsResponse>(UNREAD).toDomain() }

        assertEquals(2, counts.threadsCount)
        assertEquals(Date(1789047633686), counts.threads.single().lastRead)
    }

    @Test
    fun `Search and members payloads keep their v1 bodies`() {
        val sort = listOf(SortParamRequest(field = "created_at", direction = -1))
        val since = mapOf("created_at" to mapOf("\$gt" to Date(1791798765916)))
        assertBody(
            """{"filter_conditions":{"cid":"messaging:sample-app-channel-8"},"limit":2,""" +
                """"query":"zebra","sort":[{"direction":-1,"field":"created_at"}],""" +
                """"message_filter_conditions":{"created_at":{"${'$'}gt":"2026-10-12T09:52:45.916Z"}}}""",
            SearchPayload(
                filterConditions = mapOf("cid" to "messaging:sample-app-channel-8"),
                messageFilterConditions = since,
                query = "zebra",
                limit = 2,
                sort = sort,
            ),
        )
        assertBody(
            """{"type":"messaging",""" +
                """"filter_conditions":{"created_at":{"${'$'}gt":"2026-10-12T09:52:45.916Z"}},"id":"a",""" +
                """"limit":2,"members":[],"sort":[{"direction":-1,"field":"created_at"}]}""",
            QueryMembersPayload(type = "messaging", id = "a", filterConditions = since, limit = 2, sort = sort),
        )
    }

    @Test
    fun `A members query nests the members' custom data on v2`() {
        val payload = QueryMembersPayload(
            type = "messaging",
            members = listOf(ChannelMemberRequest(userId = "amit", custom = mapOf("k" to "v"))),
        )

        assertEquals(
            """{"type":"messaging","filter_conditions":{},"members":[{"user_id":"amit","custom":{"k":"v"}}],"sort":[]}""",
            v2Body(payload),
        )
    }

    private inline fun <reified T : Any> assertBody(expected: String, request: T) {
        assertEquals(expected, v2Body(request))
    }

    private companion object {
        private const val USER = """{"id":"filip","name":"Chewbacca","custom":{},"language":"","role":"user",
            "teams":[],"created_at":1634853490403855000,"updated_at":1791791284528654000,"banned":false,
            "online":true,"blocked_user_ids":[]}"""

        private const val SEARCH = """{"results":[{"message":{"id":"fc511a6e-2e8e-4d2c-b0a8-46b52266c39b",
            "text":"v2capsearch zebra","html":"<p>v2capsearch zebra</p>\n","type":"regular","user":$USER,
            "attachments":[],"latest_reactions":[],"own_reactions":[],"reaction_counts":{},"reaction_scores":{},
            "reply_count":0,"deleted_reply_count":0,"cid":"messaging:sample-app-channel-8",
            "created_at":1791798765916694000,"updated_at":1791798765916694000,"custom":{"s_probe":"nested"},
            "shadowed":false,"mentioned_users":[],"mentioned_channel":false,"mentioned_here":false,"silent":false,
            "pinned":false,"restricted_visibility":[]}}],"duration":"45.99ms"}"""

        private const val MEMBERS = """{"members":[{"user_id":"amit","user":{"id":"amit","name":"Leia Organa",
            "custom":{"birthland":"Tatooine"},"language":"hi","role":"user","teams":[],
            "created_at":1634853490411137000,"updated_at":1788527535982343000,"banned":false,"online":false,
            "blocked_user_ids":[]},"custom":{},"created_at":1634854188381661000,"updated_at":1786086894668787000,
            "banned":false,"shadow_banned":false,"role":"member","channel_role":"channel_member",
            "notifications_muted":false}],"duration":"9.60ms"}"""

        private const val UNREAD = """{"total_unread_count":0,"total_unread_threads_count":2,"channels":[],
            "channel_type":[],"threads":[{"unread_count":1,"last_read":1789047633686986000,"last_read_message_id":"",
            "parent_message_id":"2b1bbee3-eeb9-434c-ab78-9765abecc357"}],"duration":"7.50ms"}"""
    }
}
