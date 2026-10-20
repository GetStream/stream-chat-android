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
import io.getstream.chat.android.network.models.GroupedChannelsGroupRequest
import io.getstream.chat.android.network.models.GroupedQueryChannelsRequest
import io.getstream.chat.android.network.models.GroupedQueryChannelsResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Date

/**
 * Grouped channel queries parse their recorded v2 response through the generated [Serializer] alone, and send the same
 * request body as on v1.
 */
internal class V2LaneBatchTenTest {

    private val v1Parser = ParserFactory.createMoshiChatParser()
    private val mapping = DomainMapping(
        currentUserIdProvider = { "member_01" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
        hidesStoredDeclaredCustom = true,
    )

    private inline fun <reified T> parse(json: String): T = Serializer.moshi.adapter(T::class.java).fromJson(json)!!

    private inline fun <reified T> v2Body(request: T): String = Serializer.moshi.adapter(T::class.java).toJson(request)

    @Test
    fun `A grouped query parses its groups, nested custom data and integer dates from v2`() {
        val response = parse<GroupedQueryChannelsResponse>(GROUPED)

        assertEquals(setOf("new", "current"), response.groups.keys)
        assertEquals(emptyList<Any>(), response.groups.getValue("current").channels)
        assertEquals("cursor-new", response.groups.getValue("new").next)
        assertEquals(3, response.groups.getValue("new").unreadChannels)
        val channel = with(mapping) { response.groups.getValue("new").channels.single().channel!!.toDomain() }
        assertEquals("messaging:new-channel-1790179559146", channel.cid)
        assertEquals("New Channel", channel.name)
        assertEquals(mapOf("group" to "new"), channel.extraData - setOf("blocked", "disabled", "hidden"))
        assertEquals(Date(1790179558992), channel.createdAt)
        assertEquals(Date(1790679584051), channel.lastMessageAt)
    }

    @Test
    fun `A grouped query has the same body on v2 as on v1`() {
        val request = GroupedQueryChannelsRequest(
            limit = 5,
            presence = true,
            watch = true,
            groups = mapOf("new" to GroupedChannelsGroupRequest(limit = 2, next = "cursor")),
        )

        assertEquals(v1Parser.toJson(request), v2Body(request))
    }

    private companion object {
        /**
         * One channel state of the recorded response, without its members, messages and reads, and a shorter name.
         * The cursor and unread count are added: the recorded groups had a single page and no unread channels.
         */
        private const val GROUPED = """{"groups":{"current":{"channels":[]},"new":{"channels":[{"channel":{
            "id":"new-channel-1790179559146","type":"messaging","cid":"messaging:new-channel-1790179559146",
            "created_at":1790179558992620000,"updated_at":1790179558992620000,"last_message_at":1790679584051852000,
            "frozen":false,"disabled":false,"blocked":false,"hidden":false,"member_count":2,
            "custom":{"name":"New Channel","group":"new"}},"members":[],"messages":[],"pinned_messages":[],
            "read":[]}],"next":"cursor-new","unread_channels":3}},"duration":"21.83ms"}"""
    }
}
