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

import com.squareup.moshi.JsonAdapter
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api2.mapping.DtoMapping
import io.getstream.chat.android.models.MemberData
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.network.infrastructure.Serializer
import io.kotest.assertions.json.shouldEqualJson
import org.junit.jupiter.api.Test

internal class CreateChannelMembersSerializationTest {

    // The v2 channel endpoints serialize their bodies with the generated Serializer.
    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> toJson(value: T): String =
        (Serializer.moshi.adapter(value.javaClass) as JsonAdapter<T>).toJson(value)

    private val dtoMapping = DtoMapping(NoOpMessageTransformer, NoOpUserTransformer)

    @Test
    fun `Serialize the channel data members with their role at the root and extra data under custom`() {
        val members = listOf(
            MemberData(userId = "alice", extraData = mapOf("nickname" to "Al", "channel_role" to "channel_moderator")),
            MemberData(userId = "bob"),
        ).map { with(dtoMapping) { it.toChannelMemberRequest() } }
        val query = QueryChannelRequest()
            .withData(mapOf("name" to "Team", "members" to members))
            .apply { state = true }

        val json = toJson(with(dtoMapping) { query.toChannelGetOrCreateRequest() })

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
                "members": [
                  { "user_id": "alice", "channel_role": "channel_moderator", "custom": { "nickname": "Al" } },
                  { "user_id": "bob", "custom": {} }
                ],
                "custom": { "name": "Team" }
              }
            }
            """,
        )
    }
}
