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

import io.getstream.chat.android.network.models.ThreadParticipant
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

internal class ThreadParticipantParsingTest {
    private val parser = ParserFactory.createMoshiChatParser()

    @Test
    fun `Deserialize a thread participant as sent by the API`() {
        val participant = parser.fromJson(THREAD_PARTICIPANT_JSON, ThreadParticipant::class.java)

        participant.userId shouldBeEqualTo "leandro"
        participant.channelCid shouldBeEqualTo "messaging:channelId"
        participant.threadId shouldBeEqualTo "parentMessageId"
        participant.custom.shouldBeEmpty()
    }

    @Test
    fun `Deserialize the nested user with its custom fields collected`() {
        val participant = parser.fromJson(THREAD_PARTICIPANT_JSON, ThreadParticipant::class.java)

        participant.user?.id shouldBeEqualTo "leandro"
        participant.user?.language shouldBeEqualTo "pt"
        participant.user?.custom shouldBeEqualTo mapOf("birthland" to "Polis Massa")
    }

    companion object {
        /**
         * Captured from `POST /threads`. The API sends `custom` as an explicit `null` here, which only
         * parses thanks to [io.getstream.chat.android.client.parser2.adapters.NullCollectionsAsEmptyFactory].
         */
        private const val THREAD_PARTICIPANT_JSON =
            """{
                "app_pk": 102398,
                "channel_cid": "messaging:channelId",
                "last_thread_message_at": "2026-07-03T12:53:32.005047Z",
                "thread_id": "parentMessageId",
                "user_id": "leandro",
                "user": {
                    "id": "leandro",
                    "name": "Padmé Amidala",
                    "language": "pt",
                    "role": "user",
                    "teams": [],
                    "created_at": "2021-07-20T14:17:07.653935Z",
                    "updated_at": "2026-07-31T11:38:42.46896Z",
                    "banned": false,
                    "online": true,
                    "blocked_user_ids": [],
                    "birthland": "Polis Massa"
                },
                "created_at": "2026-07-03T12:53:25.510355Z",
                "last_read_at": "2026-07-03T12:53:32.221127Z",
                "custom": null
            }"""
    }
}
