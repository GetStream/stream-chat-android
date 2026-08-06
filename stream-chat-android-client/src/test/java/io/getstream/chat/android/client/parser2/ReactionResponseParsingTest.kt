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

import io.getstream.chat.android.network.models.GetReactionsResponse
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldHaveSize
import org.junit.jupiter.api.Test

internal class ReactionResponseParsingTest {
    private val parser = ParserFactory.createMoshiChatParser()

    @Test
    fun `Deserialize a reactions response`() {
        val reactions = parser.fromJson(REACTIONS_JSON, GetReactionsResponse::class.java).reactions

        reactions shouldHaveSize 2
        reactions.first().type shouldBeEqualTo "smile"
        reactions.first().score shouldBeEqualTo 1
        reactions.first().messageId shouldBeEqualTo "messageId"
        reactions.first().user.id shouldBeEqualTo "leandro"
    }

    @Test
    fun `Collect emoji_code and other root-level custom fields into custom`() {
        val reactions = parser.fromJson(REACTIONS_JSON, GetReactionsResponse::class.java).reactions

        // The backend sends emoji_code as custom data rather than a declared field.
        reactions.first().custom shouldBeEqualTo mapOf("emoji_code" to "😄", "weight" to 3.0)
        reactions.last().custom shouldBeEqualTo emptyMap()
    }

    companion object {
        private const val REACTIONS_JSON =
            """{
                "duration": "9ms",
                "reactions": [
                    {
                        "message_id": "messageId",
                        "user_id": "leandro",
                        "type": "smile",
                        "score": 1,
                        "created_at": "2026-06-29T08:36:59.000Z",
                        "updated_at": "2026-06-29T08:36:59.000Z",
                        "emoji_code": "😄",
                        "weight": 3,
                        "user": {
                            "id": "leandro",
                            "role": "user",
                            "language": "pt",
                            "banned": false,
                            "online": true,
                            "created_at": "2021-07-20T14:17:07.000Z",
                            "updated_at": "2026-07-31T11:38:42.000Z"
                        }
                    },
                    {
                        "message_id": "messageId",
                        "user_id": "filip",
                        "type": "sad",
                        "score": 1,
                        "created_at": "2026-06-29T08:36:52.000Z",
                        "updated_at": "2026-06-29T08:36:52.000Z",
                        "user": {
                            "id": "filip",
                            "role": "user",
                            "language": "",
                            "banned": false,
                            "online": false,
                            "created_at": "2021-10-21T21:58:10.000Z",
                            "updated_at": "2026-08-05T03:32:10.000Z"
                        }
                    }
                ]
            }"""
    }
}
