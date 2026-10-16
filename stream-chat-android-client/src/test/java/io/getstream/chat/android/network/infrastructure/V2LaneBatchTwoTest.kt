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
import io.getstream.chat.android.client.parser2.ParserFactory
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.models.UserBlock
import io.getstream.chat.android.network.models.BlockUsersRequest
import io.getstream.chat.android.network.models.BlockUsersResponse
import io.getstream.chat.android.network.models.CreateGuestRequest
import io.getstream.chat.android.network.models.CreateGuestResponse
import io.getstream.chat.android.network.models.GetBlockedUsersResponse
import io.getstream.chat.android.network.models.QueryUsersPayload
import io.getstream.chat.android.network.models.SharedLocationsResponse
import io.getstream.chat.android.network.models.SortParamRequest
import io.getstream.chat.android.network.models.UpdateUserPartialRequest
import io.getstream.chat.android.network.models.UpdateUsersPartialRequest
import io.getstream.chat.android.network.models.UpdateUsersRequest
import io.getstream.chat.android.network.models.UpdateUsersResponse
import io.getstream.chat.android.network.models.UserRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Date

/**
 * The endpoints of the second v2 batch (users, blocks, live locations, guest) parse their recorded v2 responses
 * through the generated [Serializer] alone. v2 reads a user's custom data only when it is nested under `custom`, so
 * user requests must keep it nested; the other request bodies are the same as on v1.
 */
internal class V2LaneBatchTwoTest {

    private val v1Parser = ParserFactory.createMoshiChatParser()
    private val mapping = DomainMapping(
        currentUserIdProvider = { "filip" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    private inline fun <reified T> parse(json: String): T = Serializer.moshi.adapter(T::class.java).fromJson(json)!!

    private inline fun <reified T> v2Body(request: T): String = Serializer.moshi.adapter(T::class.java).toJson(request)

    @Test
    fun `An updated user parses its nested custom data and integer dates from v2`() {
        val user = with(mapping) { parse<UpdateUsersResponse>(UPDATE_USERS).users.getValue("filip").toDomain() }

        // shadow_banned is a declared field the v1 adapter also keeps in custom data.
        assertEquals(mapOf("v2cap_probe" to "nested", "shadow_banned" to false), user.extraData)
        assertEquals(Date(1634853490403), user.createdAt)
        assertEquals(Date(1791566262411), user.lastActive)
    }

    @Test
    fun `Blocked users parse from v2`() {
        val block = with(mapping) { parse<GetBlockedUsersResponse>(BLOCKED_USERS).blocks.toDomain() }.single()

        assertEquals(
            UserBlock(
                blockedBy = "filip",
                userId = "jaewoong",
                blockedAt = Date(1791790728736),
                blockedUser = block.blockedUser,
            ),
            block,
        )
        assertEquals("jaewoong", block.blockedUser?.id)
        assertEquals("C-3PO", block.blockedUser?.name)
        assertEquals("ko", block.blockedUser?.language)
    }

    @Test
    fun `A block parses its integer date from v2`() {
        val block = with(mapping) { parse<BlockUsersResponse>(BLOCK).toDomain() }

        assertEquals(UserBlock(blockedBy = "filip", userId = "jaewoong", blockedAt = Date(1791790728736)), block)
    }

    @Test
    fun `An empty live location list parses from v2`() {
        val response = parse<SharedLocationsResponse>("""{"active_live_locations":[],"duration":"2.41ms"}""")

        assertTrue(response.activeLiveLocations.isEmpty())
    }

    @Test
    fun `A guest parses its nested custom data from v2`() {
        val response = parse<CreateGuestResponse>(GUEST)
        val user = with(mapping) { response.user.toDomain() }

        assertEquals("guest", user.role)
        assertEquals(mapOf("v2cap_probe" to "guest"), user.extraData)
    }

    @Test
    fun `A user update nests custom data on v2, where the v1 body would lose it`() {
        val request = UpdateUsersRequest(
            mapOf("filip" to UserRequest(id = "filip", name = "Chewbacca", custom = mapOf("v2cap_probe" to "nested"))),
        )

        assertEquals(
            """{"users":{"filip":{"id":"filip","name":"Chewbacca","custom":{"v2cap_probe":"nested"}}}}""",
            v2Body(request),
        )
        // v1 flattens custom data to the user's root, which a v2 endpoint silently drops.
        assertEquals(
            """{"users":{"filip":{"id":"filip","name":"Chewbacca","v2cap_probe":"nested"}}}""",
            v1Parser.toJson(request),
        )
    }

    @Test
    fun `A guest request nests custom data on v2`() {
        val request = CreateGuestRequest(UserRequest(id = "guest", name = "Guest", custom = mapOf("k" to "v")))

        assertEquals("""{"user":{"id":"guest","name":"Guest","custom":{"k":"v"}}}""", v2Body(request))
    }

    @Test
    fun `A partial user update has the same body on v2 as on v1`() {
        // Root keys of `set` are custom data on both versions.
        assertSameBody(
            UpdateUsersPartialRequest(
                listOf(UpdateUserPartialRequest(id = "filip", set = mapOf("hobby" to "chess"), unset = listOf("old"))),
            ),
        )
    }

    @Test
    fun `A users query has the same payload on v2 as on v1`() {
        assertSameBody(
            QueryUsersPayload(
                filterConditions = mapOf("id" to mapOf("\$in" to listOf("filip", "jaewoong"))),
                offset = 0,
                limit = 30,
                sort = listOf(SortParamRequest(field = "last_active", direction = -1)),
                presence = true,
            ),
        )
    }

    @Test
    fun `A block request has the same body on v2 as on v1`() {
        assertSameBody(BlockUsersRequest("jaewoong"))
    }

    private inline fun <reified T : Any> assertSameBody(request: T) {
        assertEquals(v1Parser.toJson(request), v2Body(request))
    }

    private companion object {
        /** The updated user is the own user, so it carries the own-user fields; its push devices are left out. */
        private const val UPDATE_USERS = """{"users":{"filip":{"id":"filip","name":"Chewbacca",
            "image":"https://vignette.wikia.nocookie.net/starwars/images/4/48/Chewbacca_TLJ.png",
            "custom":{"v2cap_probe":"nested"},"language":"","role":"user","teams":[],
            "created_at":1634853490403855000,"updated_at":1791790707545791000,"banned":false,"online":false,
            "last_active":1791566262411609000,"avg_response_time":1910108,
            "privacy_settings":{"typing_indicators":{"enabled":true},"read_receipts":{"enabled":true},
            "delivery_receipts":{"enabled":true}},"devices":[],"invisible":false,"mutes":[],"channel_mutes":[],
            "unread_count":0,"total_unread_count":0,"unread_channels":0,"unread_threads":0,"shadow_banned":false,
            "blocked_user_ids":[]}},"membership_deletion_task_id":"","duration":"21.84ms"}"""

        private const val V2_USER = """{"id":"filip","name":"Chewbacca","custom":{},"language":"","role":"user",
            "teams":[],"created_at":1634853490403855000,"updated_at":1791790708477080000,"banned":false,
            "online":false,"blocked_user_ids":[]}"""

        private const val BLOCKED_USER = """{"id":"jaewoong","name":"C-3PO","custom":{},"language":"ko",
            "role":"user","teams":[],"created_at":1634853490425705000,"updated_at":1790927983539826000,
            "banned":false,"online":false,"blocked_user_ids":[]}"""

        private const val BLOCKED_USERS = """{"blocks":[{"user_id":"filip","blocked_user_id":"jaewoong",
            "created_at":1791790728736999000,"user":$V2_USER,"blocked_user":$BLOCKED_USER}],"duration":"4.10ms"}"""

        private const val BLOCK = """{"duration":"110.10ms","blocked_by_user_id":"filip",
            "blocked_user_id":"jaewoong","created_at":1791790728736998538}"""

        private const val GUEST = """{"access_token":"guest-token","duration":"8.92ms","user":{
            "id":"guest-b001dfec-a0ea-4451-98c6-8bb95dc583e8-v2cap-guest","name":"v2 capture guest",
            "custom":{"v2cap_probe":"guest"},"language":"","role":"guest","teams":[],
            "created_at":1791790730737884000,"updated_at":1791790730737884000,"banned":false,"online":false,
            "blocked_user_ids":[]}}"""
    }
}
