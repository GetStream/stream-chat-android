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
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.network.models.PollResponse
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldHaveSize
import org.junit.jupiter.api.Test

internal class PollResponseParsingTest {
    private val parser = ParserFactory.createMoshiChatParser()

    private val domainMapping = DomainMapping(
        currentUserIdProvider = { null },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    @Test
    fun `Deserialize a poll response`() {
        val poll = parser.fromJson(POLL_JSON, PollResponse::class.java).poll

        poll.id shouldBeEqualTo "pollId"
        poll.name shouldBeEqualTo "Best Star Wars film"
        poll.votingVisibility shouldBeEqualTo "public"
        poll.voteCount shouldBeEqualTo 2
        poll.options shouldHaveSize 2
    }

    @Test
    fun `Collect the root-level custom fields of a poll and its options`() {
        val poll = parser.fromJson(POLL_JSON, PollResponse::class.java).poll

        poll.custom shouldBeEqualTo mapOf("category" to "cinema")
        poll.options.first().custom shouldBeEqualTo mapOf("episode" to "V")
    }

    @Test
    fun `Option custom data survives the mapping to the domain`() {
        val response = parser.fromJson(POLL_JSON, PollResponse::class.java).poll

        val poll = with(domainMapping) { response.toDomain() }

        // Collected into `custom` on the way in, and it has to reach `Option.extraData` too: the
        // hand-written DTO path and the direct path both carry it.
        poll.options.first().extraData shouldBeEqualTo mapOf("episode" to "V")
    }

    @Test
    fun `Deserialize the nested vote and its user`() {
        val poll = parser.fromJson(POLL_JSON, PollResponse::class.java).poll

        poll.ownVotes shouldHaveSize 1
        poll.ownVotes.first().optionId shouldBeEqualTo "optionA"
        poll.ownVotes.first().user?.id shouldBeEqualTo "leandro"
        poll.ownVotes.first().user?.custom shouldBeEqualTo mapOf("birthland" to "Polis Massa")
    }

    companion object {
        private const val POLL_JSON =
            """{
                "duration": "12ms",
                "poll": {
                    "id": "pollId",
                    "name": "Best Star Wars film",
                    "description": "",
                    "created_by_id": "leandro",
                    "created_at": "2026-06-10T11:04:31.000Z",
                    "updated_at": "2026-06-10T11:04:31.000Z",
                    "allow_answers": false,
                    "allow_user_suggested_options": true,
                    "answers_count": 0,
                    "enforce_unique_vote": true,
                    "vote_count": 2,
                    "voting_visibility": "public",
                    "category": "cinema",
                    "options": [
                        { "id": "optionA", "text": "The Empire Strikes Back", "episode": "V" },
                        { "id": "optionB", "text": "A New Hope" }
                    ],
                    "vote_counts_by_option": { "optionA": 2 },
                    "own_votes": [
                        {
                            "id": "voteId",
                            "poll_id": "pollId",
                            "option_id": "optionA",
                            "created_at": "2026-06-10T11:04:31.000Z",
                            "updated_at": "2026-06-10T11:04:31.000Z",
                            "user_id": "leandro",
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
                }
            }"""
    }
}
