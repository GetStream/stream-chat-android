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

import io.getstream.chat.android.client.api2.mapping.DtoMapping
import io.getstream.chat.android.client.api2.model.requests.QueryChannelRequest
import io.getstream.chat.android.models.MemberData
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.kotest.assertions.json.shouldEqualJson
import org.junit.jupiter.api.Test

internal class CreateChannelMembersSerializationTest {

    private val parser = ParserFactory.createMoshiChatParser()

    private val dtoMapping = DtoMapping(NoOpMessageTransformer, NoOpUserTransformer)

    @Test
    fun `Serialize the channel data members with their extra data flattened to the root`() {
        val members = listOf(
            MemberData(userId = "alice", extraData = mapOf("nickname" to "Al")),
            MemberData(userId = "bob"),
        ).map { with(dtoMapping) { it.toChannelMemberRequest() } }
        val request = QueryChannelRequest(
            state = true,
            watch = false,
            presence = false,
            messages = emptyMap(),
            watchers = emptyMap(),
            members = emptyMap(),
            data = mapOf("name" to "Team", "members" to members),
        )

        val json = parser.toJson(request)

        json.shouldEqualJson(
            """
            {
              "state": true,
              "watch": false,
              "presence": false,
              "messages": {},
              "watchers": {},
              "members": {},
              "data": {
                "name": "Team",
                "members": [
                  { "user_id": "alice", "nickname": "Al" },
                  { "user_id": "bob" }
                ]
              }
            }
            """,
        )
    }
}
