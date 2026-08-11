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

import io.getstream.chat.android.network.models.CreateDraftRequest
import io.getstream.chat.android.network.models.MessageRequest
import io.getstream.chat.android.network.models.SendMessageRequest
import io.getstream.chat.android.network.models.TruncateChannelRequest
import io.getstream.chat.android.network.models.UpdateMessageRequest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class MessageWriteRequestAdapterTest {
    private val parser = ParserFactory.createMoshiChatParser()

    private val message = MessageRequest(
        id = "messageId",
        text = "text",
        type = MessageRequest.Type.Regular,
        custom = mapOf("customKey" to "customValue"),
    )

    /** The nested message keeps its custom data flattened to the message root, not nested under `custom`. */
    private val messageJson =
        """{"id":"messageId","text":"text","type":"regular","attachments":[],""" +
            """"mentioned_group_ids":[],"mentioned_roles":[],"mentioned_users":[],""" +
            """"restricted_visibility":[],"customKey":"customValue"}"""

    @Test
    fun `Serialize SendMessageRequest`() {
        val json = parser.toJson(
            SendMessageRequest(message = message, skipPush = true, skipEnrichUrl = false),
        )

        Assertions.assertEquals("""{"message":$messageJson,"skip_enrich_url":false,"skip_push":true}""", json)
    }

    @Test
    fun `Serialize UpdateMessageRequest`() {
        val json = parser.toJson(
            UpdateMessageRequest(message = message, skipEnrichUrl = true, skipPush = false),
        )

        Assertions.assertEquals("""{"message":$messageJson,"skip_enrich_url":true,"skip_push":false}""", json)
    }

    @Test
    fun `Serialize CreateDraftRequest`() {
        val json = parser.toJson(CreateDraftRequest(message = message))

        Assertions.assertEquals("""{"message":$messageJson}""", json)
    }

    @Test
    fun `Serialize TruncateChannelRequest with a system message`() {
        val json = parser.toJson(TruncateChannelRequest(message = message))

        Assertions.assertEquals("""{"member_ids":[],"message":$messageJson}""", json)
    }

    @Test
    fun `Serialize TruncateChannelRequest without a message`() {
        val json = parser.toJson(TruncateChannelRequest())

        Assertions.assertEquals("""{"member_ids":[]}""", json)
    }
}
