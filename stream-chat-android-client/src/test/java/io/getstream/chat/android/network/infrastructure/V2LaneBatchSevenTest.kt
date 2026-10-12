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

import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.api2.mapping.DtoMapping
import io.getstream.chat.android.client.parser2.ParserFactory
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.network.models.ChannelMemberRequest
import io.getstream.chat.android.network.models.ChannelStateResponse
import io.getstream.chat.android.network.models.HideChannelRequest
import io.getstream.chat.android.network.models.MarkDeliveredRequest
import io.getstream.chat.android.network.models.MarkReadRequest
import io.getstream.chat.android.network.models.MarkUnreadRequest
import io.getstream.chat.android.network.models.QueryChannelsRequest
import io.getstream.chat.android.network.models.SortParamRequest
import io.getstream.chat.android.network.models.TruncateChannelRequest
import io.getstream.chat.android.network.models.UpdateChannelPartialRequest
import io.getstream.chat.android.network.models.UpdateChannelRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Date

/**
 * The channel endpoints parse their recorded v2 responses through the generated [Serializer] alone. v2 reads channel
 * and member custom data only when it is nested under `custom`, so channel data keeps it nested and sends the declared
 * keys v1 flattened next to it as typed fields; the other request bodies are the same as on v1.
 */
internal class V2LaneBatchSevenTest {

    private val v1Parser = ParserFactory.createMoshiChatParser()
    private val mapping = DomainMapping(
        currentUserIdProvider = { "filip" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )
    private val dtoMapping = DtoMapping(NoOpMessageTransformer, NoOpUserTransformer)

    private inline fun <reified T> parse(json: String): T = Serializer.moshi.adapter(T::class.java).fromJson(json)!!

    private inline fun <reified T> v2Body(request: T): String = Serializer.moshi.adapter(T::class.java).toJson(request)

    @Test
    fun `A channel state parses channel and member custom data and integer dates from v2`() {
        val state = parse<ChannelStateResponse>(STATE)

        with(mapping) {
            val channel = state.channel!!.toDomain()
            assertEquals("nested", channel.extraData["ch_probe"])
            assertEquals(Date(1791801595056), channel.createdAt)
            val member = state.members.single().toDomain()
            assertEquals("nested", member.extraData["m_probe"])
            assertEquals(Date(1791801595060), member.createdAt)
        }
    }

    @Test
    fun `Channel data nests custom data on v2 and sends the declared keys typed`() {
        val request = with(dtoMapping) {
            QueryChannelRequest().withData(
                mapOf(
                    "name" to "Channel",
                    "team" to "red",
                    "members" to listOf(ChannelMemberRequest(userId = "filip"), "jaewoong"),
                    "k" to "v",
                ),
            ).toChannelGetOrCreateRequest()
        }

        assertEquals(
            """{"presence":false,"state":false,"watch":false,"data":{"team":"red","members":[{"user_id":"filip"},""" +
                """{"user_id":"jaewoong"}],"custom":{"name":"Channel","k":"v"}},"members":{},"messages":{},""" +
                """"watchers":{}}""",
            v2Body(request),
        )
        // v1 flattens custom data into `data`, where the backend matches the declared keys first.
        assertEquals(
            """{"presence":false,"state":false,"watch":false,"data":{"team":"red","members":[{"user_id":"filip"},""" +
                """{"user_id":"jaewoong"}],"name":"Channel","k":"v"},"members":{},"messages":{},"watchers":{}}""",
            v1Parser.toJson(request),
        )
    }

    @Test
    fun `Channel update data nests custom data on v2 and sends the declared keys typed`() {
        val data = mapOf("name" to "Channel", "frozen" to true, "k" to "v")
        val request = with(dtoMapping) { UpdateChannelRequest(data = data.toChannelInputRequest()) }

        val lists = """"add_filter_tags":[],"add_members":[],"add_moderators":[],"assign_roles":[],""" +
            """"demote_moderators":[],"invites":[],"remove_filter_tags":[],"remove_members":[]"""
        assertEquals(
            """{$lists,"data":{"frozen":true,"invites":[],"members":[],"custom":{"name":"Channel","k":"v"}}}""",
            v2Body(request),
        )
        assertEquals(
            """{$lists,"data":{"frozen":true,"invites":[],"members":[],"name":"Channel","k":"v"}}""",
            v1Parser.toJson(request),
        )
    }

    @Test
    fun `The other channel requests have the same bodies on v2 as on v1`() {
        val sort = listOf(SortParamRequest(field = "last_message_at", direction = -1))
        assertSameBody(QueryChannelsRequest(filterConditions = mapOf("type" to "messaging"), sort = sort, limit = 1))
        assertSameBody(UpdateChannelPartialRequest(set = mapOf("k" to "v"), unset = listOf("q")))
        assertSameBody(MarkReadRequest(messageId = "m"))
        assertSameBody(MarkUnreadRequest(messageId = "m"))
        assertSameBody(MarkDeliveredRequest())
        assertSameBody(HideChannelRequest(clearHistory = true))
        assertSameBody(TruncateChannelRequest(skipPush = true))
        assertSameBody(emptyMap<Any, Any>())
    }

    private inline fun <reified T : Any> assertSameBody(request: T) {
        assertEquals(v1Parser.toJson(request), v2Body(request))
    }

    private companion object {
        private const val USER = """{"id":"filip","name":"Chewbacca","custom":{},"language":"","role":"user",
            "teams":[],"created_at":1634853490403855000,"updated_at":1791791284528654000,"banned":false,
            "online":true,"blocked_user_ids":[]}"""

        /** The recorded response also carries the channel's creator, config, capabilities and reads, left out here. */
        private const val STATE = """{"channel":{"id":"v2cap-channel-5479","type":"messaging",
            "cid":"messaging:v2cap-channel-5479","created_at":1791801595056562000,
            "updated_at":1791801595056562000,"frozen":false,"disabled":false,"member_count":2,"hidden":false,
            "blocked":false,"custom":{"ch_probe":"nested"}},"messages":[],"pinned_messages":[],
            "members":[{"user_id":"filip","user":$USER,"custom":{"m_probe":"nested"},"status":"member",
            "created_at":1791801595060361000,"updated_at":1791801595060361000,"banned":false,"shadow_banned":false,
            "role":"owner","channel_role":"channel_member","notifications_muted":false}],"duration":"14.34ms"}"""
    }
}
