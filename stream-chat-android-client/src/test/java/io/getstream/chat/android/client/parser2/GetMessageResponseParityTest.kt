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

package io.getstream.chat.android.client.parser2

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.api2.model.dto.DownstreamMessageDto
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.network.models.GetMessageResponse
import io.getstream.chat.android.network.models.MessageResponse
import io.getstream.chat.android.network.models.MessageWithChannelResponse
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

/**
 * Get message responses parsed through the generated [MessageWithChannelResponse], against the generated
 * [MessageResponse] it extends and the hand-written [DownstreamMessageDto] used before.
 */
internal class GetMessageResponseParityTest {

    private val parser = ParserFactory.createMoshiChatParser()
    private val mapping = DomainMapping(
        currentUserIdProvider = { "jaewoong" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    @ParameterizedTest(name = "{0}")
    @MethodSource("io.getstream.chat.android.client.parser2.MessageResponseParityTest#fixtures")
    fun `A message with its channel converts to the MessageResponse of its JSON`(name: String, json: String) {
        val message = runCatching { parser.fromJson(json, MessageResponse::class.java) }.getOrNull() ?: return
        val withChannel = parser.fromJson(json.withChannel(), MessageWithChannelResponse::class.java)

        // Compares every field, including the ones the message mapper doesn't read yet.
        assertFieldsEqual(name, message, with(mapping) { withChannel.toMessageResponse() })
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("io.getstream.chat.android.client.parser2.MessageResponseParityTest#fixtures")
    fun `A message with its channel maps to the same Message as the hand-written DTO`(name: String, json: String) {
        val legacy = runCatching {
            with(mapping) { parser.fromJson(json.withChannel(), DownstreamMessageDto::class.java).toDomain() }
        }
        val generated = runCatching {
            with(mapping) { parser.fromJson(json.withChannel(), MessageWithChannelResponse::class.java).toDomain() }
        }

        // The hand-written DTO rejects fixtures without its required collections, which the generated model defaults.
        if (legacy.isSuccess) assertFieldsEqual(name, legacy.getOrThrow(), generated.getOrThrow())
    }

    @Test
    fun `A message with its channel keeps the fields the message mapper only reads through custom`() {
        val json = message().replaceFirst(
            "{",
            """{"mml": "<mml/>", "poll_id": "poll-1", "mentioned_group_ids": ["group-1"],
            "image_labels": {"a.png": ["cat"]},
            "draft": {"channel_cid": "messaging:get-parity", "created_at": "2026-10-05T07:31:25.465Z",
            "message": {"id": "draft-1", "text": "draft"}},""",
        )
        val withChannel = parser.fromJson(json.withChannel(), MessageWithChannelResponse::class.java)

        assertFieldsEqual(
            "extra fields",
            parser.fromJson(json, MessageResponse::class.java),
            with(mapping) {
                withChannel.toMessageResponse()
            },
        )
    }

    @Test
    fun `A message takes its channel info and keeps its custom data`() {
        val json = message().replaceFirst("{", """{"probe": "x",""").withChannel()

        val message = with(mapping) { parser.fromJson(json, MessageWithChannelResponse::class.java).toDomain() }

        message.channelInfo?.cid shouldBeEqualTo "messaging:get-parity"
        message.channelInfo?.id shouldBeEqualTo "get-parity"
        message.channelInfo?.type shouldBeEqualTo "messaging"
        message.channelInfo?.name shouldBeEqualTo "Get parity"
        message.channelInfo?.memberCount shouldBeEqualTo 2
        message.extraData["probe"] shouldBeEqualTo "x"
    }

    @Test
    fun `A pending message carries the message and no metadata`() {
        val response = getMessageResponse()

        val pending = with(mapping) { response.toPendingMessage() }

        pending.message shouldBeEqualTo with(mapping) { response.message.toDomain() }
        pending.metadata shouldBeEqualTo emptyMap()
    }

    @Test
    fun `A message with its channel reads null collections as empty`() {
        val nulls = NON_NULL_COLLECTIONS.associateWith { null }
        val json = mapAdapter.toJson(mapAdapter.fromJson(message().withChannel())!! + nulls)

        val message = parser.fromJson(json, MessageWithChannelResponse::class.java)

        message.attachments shouldBeEqualTo emptyList()
        message.latestReactions shouldBeEqualTo emptyList()
        message.mentionedUsers shouldBeEqualTo emptyList()
        message.ownReactions shouldBeEqualTo emptyList()
        message.restrictedVisibility shouldBeEqualTo emptyList()
        message.reactionCounts shouldBeEqualTo emptyMap()
        message.reactionScores shouldBeEqualTo emptyMap()
    }

    @Test
    fun `A get message response without the channel or the duration is rejected`() {
        assertThrows<JsonDataException> {
            parser.fromJson("""{"message": ${message().withChannel()}}""", GetMessageResponse::class.java)
        }
        assertThrows<JsonDataException> {
            parser.fromJson(message(), MessageWithChannelResponse::class.java)
        }
    }

    internal companion object {
        private val mapAdapter = Moshi.Builder().build().adapter<Map<String, Any?>>(
            Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java),
        )

        /** The channel a get message response nests in its message, as the v1 backend sends it. */
        private val CHANNEL = mapOf(
            "id" to "get-parity", "type" to "messaging", "cid" to "messaging:get-parity",
            "created_at" to "2026-10-05T07:31:24.840Z", "updated_at" to "2026-10-05T07:31:24.840Z",
            "frozen" to false, "disabled" to false, "member_count" to 2, "name" to "Get parity",
        )

        private val NON_NULL_COLLECTIONS = listOf(
            "attachments",
            "latest_reactions",
            "mentioned_users",
            "own_reactions",
            "restricted_visibility",
            "reaction_counts",
            "reaction_scores",
        )

        private val parser = ParserFactory.createMoshiChatParser()

        /** The first shared message fixture the generated [MessageResponse] accepts. */
        private fun message(): String = MessageResponseParityTest.fixtures()
            .map { it.get()[1] as String }
            .first { runCatching { parser.fromJson(it, MessageResponse::class.java) }.isSuccess }

        private fun String.withChannel(): String =
            mapAdapter.toJson(mapAdapter.fromJson(this)!! + ("channel" to CHANNEL))

        /** A get message response built from a shared message fixture and its channel. */
        fun getMessageResponse(): GetMessageResponse = parser.fromJson(
            """{"duration": "1ms", "message": ${message().withChannel()}}""",
            GetMessageResponse::class.java,
        )
    }
}
