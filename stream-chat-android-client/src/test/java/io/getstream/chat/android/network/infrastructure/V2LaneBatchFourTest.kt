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
import io.getstream.chat.android.client.api2.mapping.DtoMapping
import io.getstream.chat.android.client.parser2.ParserFactory
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.network.models.CreateDraftRequest
import io.getstream.chat.android.network.models.CreateDraftResponse
import io.getstream.chat.android.network.models.CreateReminderRequest
import io.getstream.chat.android.network.models.CreateReminderResponse
import io.getstream.chat.android.network.models.GetThreadResponse
import io.getstream.chat.android.network.models.MessageRequest
import io.getstream.chat.android.network.models.QueryDraftsRequest
import io.getstream.chat.android.network.models.QueryDraftsResponse
import io.getstream.chat.android.network.models.QueryRemindersRequest
import io.getstream.chat.android.network.models.QueryRemindersResponse
import io.getstream.chat.android.network.models.QueryThreadsRequest
import io.getstream.chat.android.network.models.QueryThreadsResponse
import io.getstream.chat.android.network.models.SortParamRequest
import io.getstream.chat.android.network.models.UpdateReminderRequest
import io.getstream.chat.android.network.models.UpdateThreadPartialRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.Date
import io.getstream.chat.android.models.Attachment as DomainAttachment

/**
 * The endpoints of the fourth v2 batch (threads, drafts, reminders) parse their recorded v2 responses through the
 * generated [Serializer] alone. v2 reads a draft message's custom data only when it is nested under `custom`, so
 * draft requests keep it nested; the other request bodies are the same as on v1.
 */
internal class V2LaneBatchFourTest {

    private val v1Parser = ParserFactory.createMoshiChatParser()
    private val mapping = DomainMapping(
        currentUserIdProvider = { "filip" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
        hidesStoredDeclaredCustom = true,
    )

    private val dtoMapping = DtoMapping(NoOpMessageTransformer, NoOpUserTransformer)

    private inline fun <reified T> parse(json: String): T = Serializer.moshi.adapter(T::class.java).fromJson(json)!!

    private inline fun <reified T> v2Body(request: T): String = Serializer.moshi.adapter(T::class.java).toJson(request)

    @Test
    fun `A thread parses its parent message and integer dates from v2`() {
        val thread = with(mapping) { parse<GetThreadResponse>(THREAD).thread.toDomain()!! }

        assertEquals("5ffe15d8-e5c1-43b0-908b-db906fab4996", thread.parentMessage.id)
        assertEquals(Date(1791796892900), thread.parentMessage.createdAt)
        assertEquals(Date(1791796893245), thread.lastMessageAt)
        assertEquals(listOf("filip"), thread.threadParticipants.map { it.user.id })
    }

    @Test
    fun `A draft parses its nested custom data from v2`() {
        val draft = with(mapping) { parse<CreateDraftResponse>(DRAFT).draft.toDomain() }

        assertEquals("v2cap draft", draft.text)
        assertEquals(mapOf("draft_probe" to "nested"), draft.extraData)
    }

    @Test
    fun `A reminder parses its integer dates from v2`() {
        val reminder = with(mapping) { parse<CreateReminderResponse>(REMINDER).reminder.toDomain() }

        assertEquals(Date(1798797600000), reminder.remindAt)
        assertEquals(Date(1791796896185), reminder.createdAt)
    }

    @Test
    fun `A reminder without a remind date parses from v2`() {
        val json = REMINDER.replace("\"remind_at\":1798797600000000000", "\"remind_at\":null")

        val reminder = with(mapping) { parse<CreateReminderResponse>(json).reminder.toDomain() }

        assertNull(reminder.remindAt)
    }

    @Test
    fun `Thread, draft and reminder queries parse from v2`() {
        val threads = parse<QueryThreadsResponse>("""{"threads":[${THREAD.thread()}],"duration":"25.37ms"}""")
        val drafts = parse<QueryDraftsResponse>("""{"drafts":[${DRAFT.draft()}],"duration":"6.88ms"}""")
        val reminders = parse<QueryRemindersResponse>("""{"reminders":[${REMINDER.reminder()}],"duration":"9.80ms"}""")

        with(mapping) {
            assertEquals(Date(1791796893245), threads.threads.single().toDomain()!!.createdAt)
            assertEquals(mapOf("draft_probe" to "nested"), drafts.drafts.single().toDomain().extraData)
            assertEquals(Date(1798797600000), reminders.toDomain().reminders.single().remindAt)
        }
    }

    @Test
    fun `A draft attachment sends its declared fields typed and the others nested on v2`() {
        val attachment = DomainAttachment(
            type = "file",
            assetUrl = "https://example.com/f.txt",
            name = "f.txt",
            fileSize = 2048,
            extraData = mapOf("color" to "red", "k" to "v"),
        )
        val request = with(dtoMapping) { attachment.toAttachmentRequest() }

        assertEquals(
            """{"custom":{"k":"v","name":"f.txt","file_size":2048},"asset_url":"https://example.com/f.txt",""" +
                """"color":"red","type":"file"}""",
            Serializer.moshi.adapter(io.getstream.chat.android.network.models.Attachment::class.java).toJson(request),
        )
    }

    @Test
    fun `A draft creation nests the message custom data on v2, where the v1 body would lose it`() {
        val request = CreateDraftRequest(MessageRequest(text = "Draft", custom = mapOf("k" to "v")))
        val lists = """"attachments":[],"mentioned_group_ids":[],"mentioned_roles":[],"mentioned_users":[],""" +
            """"restricted_visibility":[]"""

        assertEquals("""{"message":{"text":"Draft",$lists,"custom":{"k":"v"}}}""", v2Body(request))
        // v1 flattens custom data to the message's root, which a v2 endpoint silently drops.
        assertEquals("""{"message":{"text":"Draft",$lists,"k":"v"}}""", v1Parser.toJson(request))
    }

    @Test
    fun `Reminder requests have the same bodies on v2 as on v1`() {
        assertSameBody(CreateReminderRequest(remindAt = Date(1798797600000)))
        assertSameBody(UpdateReminderRequest(remindAt = Date(1798797600000)))
        assertSameBody(QueryRemindersRequest(filter = mapOf("channel_cid" to "messaging:a"), limit = 2))
    }

    @Test
    fun `Thread and draft queries have the same bodies on v2 as on v1`() {
        val sort = listOf(SortParamRequest(field = "created_at", direction = -1))
        assertSameBody(QueryThreadsRequest(limit = 1, replyLimit = 1, participantLimit = 1, watch = true, sort = sort))
        assertSameBody(QueryDraftsRequest(filter = mapOf("channel_cid" to "messaging:a"), limit = 5, sort = sort))
    }

    @Test
    fun `A partial thread update has the same body on v2 as on v1`() {
        // Root keys of `set` are custom data on both versions.
        assertSameBody(UpdateThreadPartialRequest(set = mapOf("title" to "Title", "k" to "v"), unset = listOf("q")))
    }

    private inline fun <reified T : Any> assertSameBody(request: T) {
        assertEquals(v1Parser.toJson(request), v2Body(request))
    }

    private companion object {
        private fun String.inner(key: String): String =
            substringAfter("{\"$key\":").substringBeforeLast("\"duration\"").trimEnd().removeSuffix(",")
        private fun String.thread() = inner("thread")
        private fun String.draft() = inner("draft")
        private fun String.reminder() = inner("reminder")

        private const val USER = """{"id":"filip","name":"Chewbacca","custom":{},"language":"","role":"user",
            "teams":[],"created_at":1634853490403855000,"updated_at":1791791284528654000,"banned":false,
            "online":true,"blocked_user_ids":[]}"""

        private const val PARENT = """{"id":"5ffe15d8-e5c1-43b0-908b-db906fab4996","text":"v2cap thread parent",
            "html":"<p>v2cap thread parent</p>\n","type":"regular","user":$USER,"attachments":[],
            "latest_reactions":[],"own_reactions":[],"reaction_counts":{},"reaction_scores":{},"reply_count":1,
            "deleted_reply_count":0,"cid":"messaging:sample-app-channel-8","created_at":1791796892900813000,
            "updated_at":1791796893252325000,"custom":{},"shadowed":false,"mentioned_users":[],
            "mentioned_channel":false,"mentioned_here":false,"silent":false,"pinned":false,
            "restricted_visibility":[]}"""

        /** The recorded response also carries the channel and the latest reply, left out here. */
        private const val THREAD = """{"thread":{"channel_cid":"messaging:sample-app-channel-8",
            "parent_message_id":"5ffe15d8-e5c1-43b0-908b-db906fab4996","parent_message":$PARENT,
            "created_by_user_id":"filip","created_by":$USER,"reply_count":1,"participant_count":1,
            "active_participant_count":0,"thread_participants":[{"app_pk":102398,
            "channel_cid":"messaging:sample-app-channel-8","last_thread_message_at":1791796893245424000,
            "thread_id":"5ffe15d8-e5c1-43b0-908b-db906fab4996","user_id":"filip","user":$USER,
            "created_at":1791796893245424000,"last_read_at":1791796893245424000,"custom":{}}],
            "last_message_at":1791796893245424000,"created_at":1791796893245424000,
            "updated_at":1791796893245424000,"title":"v2cap thread parent","custom":{},"latest_replies":[]},
            "duration":"29.23ms"}"""

        /** The recorded response also carries the channel, left out here. */
        private const val DRAFT = """{"draft":{"message":{"id":"","text":"v2cap draft",
            "custom":{"draft_probe":"nested"}},"channel_cid":"messaging:sample-app-channel-8",
            "parent_message":null,"quoted_message":null,"created_at":1791796894752884938},"duration":"14.69ms"}"""

        /** The recorded response also carries the message and the user, left out here. */
        private const val REMINDER = """{"reminder":{"remind_at":1798797600000000000,"expires_at":null,
            "channel_cid":"messaging:sample-app-channel-8","message_id":"5ffe15d8-e5c1-43b0-908b-db906fab4996",
            "user_id":"filip","created_at":1791796896185451686,"updated_at":1791796896185451686},
            "duration":"19.28ms"}"""
    }
}
