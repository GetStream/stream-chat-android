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

import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.api2.model.dto.DownstreamMessageDto
import io.getstream.chat.android.client.parser2.testdata.MessageDtoTestData
import io.getstream.chat.android.client.parser2.testdata.MessageTestData
import io.getstream.chat.android.client.parser2.testdata.WireShape
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.network.models.MessageResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.lang.reflect.Modifier

/**
 * Parses the same message JSON through the hand-written [DownstreamMessageDto] and the generated
 * [MessageResponse], and requires the two domain [Message]s to match field by field.
 */
internal class MessageResponseParityTest {

    private val parser = ParserFactory.createMoshiChatParser()
    private val mapping = DomainMapping(
        currentUserIdProvider = { "user-1" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    @ParameterizedTest(name = "{0}")
    @MethodSource("fixtures")
    fun `The generated MessageResponse maps to the same Message as the hand-written DTO`(name: String, json: String) {
        val legacy = runCatching {
            with(mapping) { parser.fromJson(json, DownstreamMessageDto::class.java).toDomain() }
        }
        val generated = runCatching {
            with(mapping) { parser.fromJson(json, MessageResponse::class.java).toDomain() }
        }
        // The generated model may accept input the hand-written DTO rejected, never the reverse.
        if (legacy.isFailure) return
        assertTrue(generated.isSuccess) { "$name: only the generated path failed: ${generated.exceptionOrNull()}" }
        val differences = diff(legacy.getOrThrow(), generated.getOrThrow())
        if (differences.isNotEmpty()) fail<Unit>("$name differs:\n" + differences.joinToString("\n"))
    }

    @Test
    fun `A message by a deleted user parses with the placeholder the backend sends`() {
        val message = with(mapping) { parser.fromJson(DELETED_AUTHOR_JSON, MessageResponse::class.java).toDomain() }

        assertEquals("deleted-user", message.user.id)
        assertEquals("", message.user.language)
        // The exact instant depends on the calendar the date parser uses for year 1; it only has to parse.
        assertTrue((message.user.createdAt?.time ?: 0) < 0) { "createdAt = ${message.user.createdAt}" }
    }

    private fun diff(legacy: Message, generated: Message): List<String> =
        Message::class.java.declaredFields
            .filterNot { Modifier.isStatic(it.modifiers) }
            .onEach { it.isAccessible = true }
            .mapNotNull { field ->
                val a = field.get(legacy)
                val b = field.get(generated)
                if (a == b) null else "  ${field.name}:\n    legacy    = $a\n    generated = $b"
            }

    companion object {

        /**
         * A message whose author was deleted: the backend substitutes a placeholder user and sends its zero
         * fields as-is (NewUserResponseOrDeletedUser in commonpayloads/user.go).
         */
        private val DELETED_AUTHOR_JSON = """
            {
              "id": "msg-deleted-author", "cid": "messaging:general", "text": "hi", "html": "<p>hi</p>",
              "type": "regular", "attachments": [], "latest_reactions": [], "own_reactions": [],
              "mentioned_users": [], "reply_count": 0, "deleted_reply_count": 0, "silent": false,
              "shadowed": false, "mentioned_channel": false, "mentioned_here": false, "pinned": false,
              "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z",
              "user": {
                "id": "deleted-user", "name": "Deleted User", "role": "", "language": "",
                "banned": false, "online": false,
                "created_at": "0001-01-01T00:00:00Z", "updated_at": "0001-01-01T00:00:00Z"
              }
            }
        """.trimIndent()

        /** Every JSON fixture that describes a message, keyed by its property name. */
        @JvmStatic
        fun fixtures(): List<Arguments> {
            val messageTestData = MessageTestData::class.java.declaredFields
                .filter { it.type == String::class.java }
                .onEach { it.isAccessible = true }
                .mapNotNull { field ->
                    (field.get(MessageTestData) as? String)?.let { "MessageTestData.${field.name}" to it }
                }
                .filter { (_, json) -> json.trimStart().startsWith("{") }
            val dtoTestData = listOf(
                "MessageDtoTestData.downstreamJson" to MessageDtoTestData.downstreamJson,
                "MessageDtoTestData.downstreamJsonWithoutExtraData" to
                    MessageDtoTestData.downstreamJsonWithoutExtraData,
                "deleted author" to DELETED_AUTHOR_JSON,
                "restricted visibility" to MessageDtoTestData.downstreamJsonWithoutExtraData.replaceFirst(
                    "{",
                    """{"restricted_visibility": ["user-a", "user-b"],""",
                ),
            )
            return (messageTestData + dtoTestData).map { (name, json) -> Arguments.of(name, WireShape.message(json)) }
        }
    }
}
