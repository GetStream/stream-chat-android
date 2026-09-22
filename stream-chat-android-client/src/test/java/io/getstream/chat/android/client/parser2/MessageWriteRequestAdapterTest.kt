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

import io.getstream.chat.android.client.parser2.testdata.MessageDtoTestData.upstreamMessageWithoutExtraData
import io.getstream.chat.android.network.models.SendMessageRequest
import io.getstream.chat.android.network.models.TruncateChannelRequest
import io.getstream.chat.android.network.models.UpdateMessageRequest
import io.kotest.assertions.json.shouldEqualJson
import org.junit.jupiter.api.Test

/**
 * The write-request wrappers declare their flags nullable, so what reaches the wire depends on whether
 * they are set. These pin the emitted JSON, which object equality in the api tests does not cover.
 */
internal class MessageWriteRequestAdapterTest {

    private val parser = ParserFactory.createMoshiChatParser()

    @Test
    fun `Serialize a send message request with both flags set`() {
        val request = SendMessageRequest(
            message = upstreamMessageWithoutExtraData,
            skipPush = true,
            skipEnrichUrl = true,
        )

        parser.toJson(request).shouldEqualJson(
            """{
                "message": $MESSAGE_JSON,
                "skip_push": true,
                "skip_enrich_url": true
            }""",
        )
    }

    /**
     * The draft path constructs the request without the flags. They are omitted rather than sent as
     * false, which the backend treats the same: both are `*bool` read through a nil-safe deref.
     */
    @Test
    fun `Serialize a send message request without the flags`() {
        val request = SendMessageRequest(message = upstreamMessageWithoutExtraData)

        parser.toJson(request).shouldEqualJson("""{"message": $MESSAGE_JSON}""")
    }

    @Test
    fun `Serialize an update message request`() {
        val request = UpdateMessageRequest(
            message = upstreamMessageWithoutExtraData,
            skipEnrichUrl = false,
            skipPush = false,
        )

        parser.toJson(request).shouldEqualJson(
            """{
                "message": $MESSAGE_JSON,
                "skip_enrich_url": false,
                "skip_push": false
            }""",
        )
    }

    /**
     * `member_ids` defaults to an empty list on the generated model rather than null, so truncate now
     * sends it where it previously sent nothing. The backend branches on `len(MemberIDs) > 0`, so an
     * empty array and an absent key both take the whole-channel path.
     */
    @Test
    fun `Serialize a truncate channel request with a system message`() {
        val request = TruncateChannelRequest(message = upstreamMessageWithoutExtraData)

        parser.toJson(request).shouldEqualJson(
            """{
                "message": $MESSAGE_JSON,
                "member_ids": []
            }""",
        )
    }

    /**
     * Truncate carries no message when the caller passes no system one, and the fields the domain has no
     * source for stay absent rather than being invented.
     */
    @Test
    fun `Serialize an empty truncate channel request`() {
        parser.toJson(TruncateChannelRequest()).shouldEqualJson("""{"member_ids": []}""")
    }

    private companion object {
        private val MESSAGE_JSON =
            ParserFactory.createMoshiChatParser().toJson(upstreamMessageWithoutExtraData)
    }
}
