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
import io.getstream.chat.android.network.models.ChannelMemberResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class MembershipParsingTest {

    @Test
    fun `a membership without user_id is rejected`() {
        val json = """
            {
              "banned": false,
              "channel_role": "channel_member",
              "created_at": "2026-10-01T10:00:00.000Z",
              "notifications_muted": false,
              "shadow_banned": false,
              "updated_at": "2026-10-01T10:00:00.000Z",
              "user": {
                "id": "luke",
                "banned": false,
                "created_at": "2026-01-01T10:00:00.000Z",
                "language": "en",
                "online": false,
                "role": "user",
                "updated_at": "2026-01-01T10:00:00.000Z"
              }
            }
        """.trimIndent()

        assertThrows<JsonDataException> { parse<ChannelMemberResponse>(json) }
    }
}
