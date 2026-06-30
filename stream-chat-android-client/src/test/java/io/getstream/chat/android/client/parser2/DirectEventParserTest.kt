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

import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.parser2.DirectEventParser.Companion.extractType
import io.getstream.chat.android.client.parser2.testdata.NewMessageEventTestData
import io.getstream.chat.android.models.MessageTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.models.UserTransformer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class DirectEventParserTest {

    private val parser = DirectEventParser(
        currentUserIdProvider = { "" },
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    @Nested
    inner class ExtractType {

        @Test
        fun `returns type when it is the first field`() {
            val json = """{"type":"message.new","cid":"messaging:general"}"""
            assertEquals("message.new", extractType(json))
        }

        @Test
        fun `returns type when it is not the first field`() {
            val json = """{"cid":"messaging:general","type":"message.new","user":{}}"""
            assertEquals("message.new", extractType(json))
        }

        @Test
        fun `returns null for missing type field`() {
            val json = """{"cid":"messaging:general","user":{}}"""
            assertNull(extractType(json))
        }

        @Test
        fun `returns null for null type value`() {
            val json = """{"type":null,"cid":"messaging:general"}"""
            assertNull(extractType(json))
        }

        @Test
        fun `returns null for empty string`() {
            assertNull(extractType(""))
        }

        @Test
        fun `returns null for non-object JSON`() {
            assertNull(extractType("[]"))
            assertNull(extractType("\"hello\""))
            assertNull(extractType("42"))
        }

        @Test
        fun `returns empty string for empty type value`() {
            val json = """{"type":"","cid":"messaging:general"}"""
            assertEquals("", extractType(json))
        }
    }

    @Nested
    inner class Parse {

        @Test
        fun `returns NewMessageEvent for message_new type`() {
            val event = parser.parse(NewMessageEventTestData.jsonAllFields)
            assertTrue(event is NewMessageEvent)
        }

        @Test
        fun `returns null for unsupported event type`() {
            val json = """{"type":"message.read","cid":"messaging:general","user":{}}"""
            assertNull(parser.parse(json))
        }

        @Test
        fun `returns null when type field is missing`() {
            val json = """{"cid":"messaging:general","user":{}}"""
            assertNull(parser.parse(json))
        }

        @Test
        fun `returns null when type is null`() {
            val json = """{"type":null,"cid":"messaging:general"}"""
            assertNull(parser.parse(json))
        }
    }

    @Nested
    inner class TransformerWiring {

        @Test
        fun `custom MessageTransformer is applied to parsed NewMessageEvent`() {
            val customParser = DirectEventParser(
                currentUserIdProvider = { "" },
                messageTransformer = MessageTransformer { it.copy(text = it.text + " [transformed]") },
                userTransformer = NoOpUserTransformer,
            )
            val event = customParser.parse(NewMessageEventTestData.jsonAllFields) as NewMessageEvent
            assertTrue(event.message.text.endsWith(" [transformed]"))
        }

        @Test
        fun `custom UserTransformer is applied to all nested users in parsed NewMessageEvent`() {
            val customParser = DirectEventParser(
                currentUserIdProvider = { "" },
                messageTransformer = NoOpMessageTransformer,
                userTransformer = UserTransformer { it.copy(name = it.name + " [transformed]") },
            )
            val event = customParser.parse(NewMessageEventTestData.jsonAllFields) as NewMessageEvent

            // Event-level user
            assertTrue(event.user.name.endsWith(" [transformed]"))
            // Message-level user
            assertTrue(event.message.user.name.endsWith(" [transformed]"))
            // Nested users within message
            event.message.mentionedUsers.forEach { assertTrue(it.name.endsWith(" [transformed]")) }
            event.message.threadParticipants.forEach { assertTrue(it.name.endsWith(" [transformed]")) }
        }
    }
}
