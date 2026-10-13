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
import io.getstream.chat.android.network.models.DeleteMessageResponse
import io.getstream.chat.android.network.models.GetMessageResponse
import io.getstream.chat.android.network.models.GetRepliesResponse
import io.getstream.chat.android.network.models.MessageActionRequest
import io.getstream.chat.android.network.models.MessageRequest
import io.getstream.chat.android.network.models.QueryReactionsRequest
import io.getstream.chat.android.network.models.ReactionRequest
import io.getstream.chat.android.network.models.SendMessageRequest
import io.getstream.chat.android.network.models.SendMessageResponse
import io.getstream.chat.android.network.models.SendReactionRequest
import io.getstream.chat.android.network.models.SendReactionResponse
import io.getstream.chat.android.network.models.SortParamRequest
import io.getstream.chat.android.network.models.TranslateMessageRequest
import io.getstream.chat.android.network.models.TranslateMessageResponse
import io.getstream.chat.android.network.models.UpdateMessagePartialRequest
import io.getstream.chat.android.network.models.UpdateMessageRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Date

/**
 * The message endpoints (send, get, update, delete, reactions, replies, translate, actions) parse their recorded v2
 * responses through the generated [Serializer] alone. v2 reads message and reaction custom data only when it is nested
 * under `custom`, so those requests keep it nested; the other request bodies are the same as on v1.
 */
internal class V2LaneBatchFiveTest {

    private val v1Parser = ParserFactory.createMoshiChatParser()
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
    fun `A sent message parses its nested custom data and integer dates from v2`() {
        val message = with(mapping) { parse<SendMessageResponse>(SENT).message.toDomain() }

        assertEquals(mapOf("msg_probe" to "nested"), message.extraData)
        assertEquals(Date(1791797467710), message.createdAt)
        assertEquals(Date(1634853490403), message.user.createdAt)
    }

    @Test
    fun `A fetched message parses its channel's name and image from the channel's nested custom data`() {
        val message = with(mapping) { parse<GetMessageResponse>(FETCHED).message.toDomain() }

        assertEquals("Silent Active", message.channelInfo?.name)
        assertEquals(mapOf("msg_probe" to "nested"), message.extraData)
    }

    @Test
    fun `Replies and a deleted message parse from v2`() {
        val reply = message("v2cap reply", "{}")
        val replies = parse<GetRepliesResponse>("""{"messages":[$reply],"duration":"19.07ms"}""")
        val gone = message("v2cap msg 3", "{}")
        val deleted = parse<DeleteMessageResponse>("""{"message":$gone,"duration":"32.91ms"}""")

        with(mapping) {
            assertEquals(Date(1791797467710), replies.messages.single().toDomain().createdAt)
            assertEquals("v2cap msg 3", deleted.message.toDomain().text)
        }
    }

    @Test
    fun `A reaction parses its nested custom data from v2`() {
        val reaction = with(mapping) { parse<SendReactionResponse>(REACTION).reaction.toDomain() }

        assertEquals(mapOf("r_probe" to "nested"), reaction.extraData)
        assertEquals(Date(1791797468742), reaction.createdAt)
    }

    @Test
    fun `A translation parses from v2`() {
        val message = with(mapping) { parse<TranslateMessageResponse>(TRANSLATED).message.toDomain() }

        assertEquals(mapOf("en_text" to "v2cap msg 3", "it_text" to "v2cap msg 3", "language" to "en"), message.i18n)
    }

    @Test
    fun `A sent message nests custom data on v2, where the v1 body would lose it`() {
        val request = SendMessageRequest(MessageRequest(text = "Hi", custom = mapOf("k" to "v")))
        val lists = """"attachments":[],"mentioned_group_ids":[],"mentioned_roles":[],"mentioned_users":[],""" +
            """"restricted_visibility":[]"""

        assertEquals("""{"message":{"text":"Hi",$lists,"custom":{"k":"v"}}}""", v2Body(request))
        // v1 flattens custom data to the message's root, which a v2 endpoint silently drops.
        assertEquals("""{"message":{"text":"Hi",$lists,"k":"v"}}""", v1Parser.toJson(request))
    }

    @Test
    fun `An updated message nests custom data on v2`() {
        val request = UpdateMessageRequest(MessageRequest(id = "m", text = "Hi", custom = mapOf("k" to "v")))

        assertEquals(
            """{"message":{"id":"m","text":"Hi","attachments":[],"mentioned_group_ids":[],"mentioned_roles":[],""" +
                """"mentioned_users":[],"restricted_visibility":[],"custom":{"k":"v"}}}""",
            v2Body(request),
        )
    }

    @Test
    fun `A reaction nests custom data on v2, where the v1 body would lose it`() {
        val reaction = ReactionRequest(type = "like", custom = mapOf("k" to "v"))
        val request = SendReactionRequest(reaction, enforceUnique = true)

        assertEquals("""{"reaction":{"type":"like","custom":{"k":"v"}},"enforce_unique":true}""", v2Body(request))
        // v1 flattens custom data to the reaction's root, which a v2 endpoint silently drops.
        assertEquals("""{"reaction":{"type":"like","k":"v"},"enforce_unique":true}""", v1Parser.toJson(request))
    }

    @Test
    fun `A partial message update has the same body on v2 as on v1`() {
        // Root keys of `set` are custom data on both versions.
        assertSameBody(UpdateMessagePartialRequest(set = mapOf("text" to "Hi", "k" to "v"), unset = listOf("q")))
    }

    @Test
    fun `Reaction queries, actions and translations have the same bodies on v2 as on v1`() {
        val sort = listOf(SortParamRequest(field = "created_at", direction = -1))
        assertSameBody(QueryReactionsRequest(filter = mapOf("type" to "like"), limit = 5, sort = sort))
        assertSameBody(MessageActionRequest(formData = mapOf("image_action" to "shuffle")))
        assertSameBody(TranslateMessageRequest(TranslateMessageRequest.Language.fromString("it")))
    }

    private inline fun <reified T : Any> assertSameBody(request: T) {
        assertEquals(v1Parser.toJson(request), v2Body(request))
    }

    private companion object {
        private const val USER = """{"id":"filip","name":"Chewbacca","custom":{},"language":"","role":"user",
            "teams":[],"created_at":1634853490403855000,"updated_at":1791791284528654000,"banned":false,
            "online":true,"blocked_user_ids":[]}"""

        private fun message(text: String, custom: String, i18n: String = "") = """{
            "id":"bad03680-ef84-45dd-ae67-e5f9ee871dff","text":"$text","html":"<p>$text</p>\n","type":"regular",
            "user":$USER,"attachments":[],"latest_reactions":[],"own_reactions":[],"reaction_counts":{},
            "reaction_scores":{},"reply_count":0,"deleted_reply_count":0,"cid":"messaging:sample-app-channel-8",
            "created_at":1791797467710999000,"updated_at":1791797467710999000,"custom":$custom,$i18n"shadowed":false,
            "mentioned_users":[],"mentioned_channel":false,"mentioned_here":false,"silent":false,"pinned":false,
            "restricted_visibility":[]}"""

        private val SENT = """{"message":${message("v2cap msg", """{"msg_probe":"nested"}""")},"duration":"32.50ms"}"""

        private val FETCHED = """{"message":${message("v2cap msg", """{"msg_probe":"nested"}""").dropLast(1)},
            "channel":{"id":"sample-app-channel-8","type":"messaging","cid":"messaging:sample-app-channel-8",
            "last_message_at":1791797467710999000,"created_at":1634854188377760000,"updated_at":1785561493155836000,
            "frozen":false,"disabled":false,"member_count":13,"truncated_at":1636539502702175000,
            "custom":{"name":"Silent Active","image":"https://picsum.photos/seed/72/100/100"}}},"duration":"13.05ms"}"""

        private val TRANSLATED = """{"message":${message(
            "v2cap msg 3",
            """{"partial_probe":"root"}""",
            """"i18n":{"en_text":"v2cap msg 3","it_text":"v2cap msg 3","language":"en"},""",
        )},"duration":"214.63ms"}"""

        private val REACTION = """{"message":${message("v2cap msg 3", """{"partial_probe":"root"}""")},
            "reaction":{"message_id":"bad03680-ef84-45dd-ae67-e5f9ee871dff","user_id":"filip","user":$USER,
            "type":"like","score":1,"created_at":1791797468742013000,"updated_at":1791797468742013000,
            "custom":{"r_probe":"nested"}},"duration":"28.71ms"}"""
    }
}
