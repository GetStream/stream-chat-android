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

import io.getstream.chat.android.network.models.MembersResponse
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldHaveSize
import org.junit.jupiter.api.Test

internal class ChannelMemberResponseParsingTest {
    private val parser = ParserFactory.createMoshiChatParser()

    @Test
    fun `Parse the members of a MembersResponse`() {
        val members = parser.fromJson(MEMBERS_JSON, MembersResponse::class.java).members

        members shouldHaveSize 1
        members.first().channelRole shouldBeEqualTo "channel_member"
        members.first().userId shouldBeEqualTo "leandro"
        members.first().custom["memberProbe"] shouldBeEqualTo "sentinel"
    }

    @Test
    fun `Collect the root-level custom fields of a nested user`() {
        val member = parser.fromJson(MEMBERS_JSON, MembersResponse::class.java).members.first()

        member.user?.custom shouldBeEqualTo mapOf("birthland" to "Polis Massa")
    }

    companion object {
        private const val MEMBERS_JSON =
            """{
                "duration": "7ms",
                "members": [
                    {
                        "user_id": "leandro",
                        "channel_role": "channel_member",
                        "created_at": "2021-10-22T00:07:24.000Z",
                        "updated_at": "2026-05-28T07:40:11.000Z",
                        "banned": false,
                        "shadow_banned": false,
                        "notifications_muted": false,
                        "role": "member",
                        "is_moderator": true,
                        "deleted_at": "2026-08-14T12:00:00.000Z",
                        "deleted_messages": ["m1"],
                        "memberProbe": "sentinel",
                        "user": {
                            "id": "leandro",
                            "role": "user",
                            "language": "pt",
                            "banned": false,
                            "online": true,
                            "created_at": "2021-07-20T14:17:07.000Z",
                            "updated_at": "2026-07-31T11:38:42.000Z",
                            "birthland": "Polis Massa"
                        }
                    }
                ]
            }"""
    }
}
