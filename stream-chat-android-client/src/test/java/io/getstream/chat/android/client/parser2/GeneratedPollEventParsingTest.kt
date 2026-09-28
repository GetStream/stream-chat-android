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
import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.api2.mapping.EventMapping
import io.getstream.chat.android.client.api2.model.response.SyncHistoryResponse
import io.getstream.chat.android.client.createPollAnswerVoteJsonString
import io.getstream.chat.android.client.createPollJsonString
import io.getstream.chat.android.client.createPollVoteJsonString
import io.getstream.chat.android.client.events.AnswerCastedEvent
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.HasPoll
import io.getstream.chat.android.client.events.PollClosedEvent
import io.getstream.chat.android.client.events.PollDeletedEvent
import io.getstream.chat.android.client.events.PollUpdatedEvent
import io.getstream.chat.android.client.events.VoteCastedEvent
import io.getstream.chat.android.client.events.VoteChangedEvent
import io.getstream.chat.android.client.events.VoteRemovedEvent
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KClass

/** Poll events parsed through their generated models, on both the WebSocket and the `/sync` path. */
internal class GeneratedPollEventParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()
    private val eventMapping = EventMapping(
        DomainMapping(
            currentUserIdProvider = { "" },
            channelTransformer = NoOpChannelTransformer,
            messageTransformer = NoOpMessageTransformer,
            userTransformer = NoOpUserTransformer,
        ),
    )

    @ParameterizedTest
    @MethodSource("events")
    fun `A WebSocket poll event maps to its domain event and keeps the created_at string as sent`(
        type: String,
        vote: String?,
        expected: KClass<out ChatEvent>,
    ) {
        val event = parser.fromJson(pollEventJson(type, vote), ChatEvent::class.java)

        event::class shouldBeEqualTo expected
        event.rawCreatedAt shouldBeEqualTo NANOSECOND_CREATED_AT
        event.createdAt.time shouldBeEqualTo CREATED_AT_MILLIS
        (event as HasPoll).poll.id shouldBeEqualTo "poll-id"
    }

    @Test
    fun `The domain event carries the cid, channel and message ids`() {
        val event = parser.fromJson(pollEventJson("poll.closed"), ChatEvent::class.java)
            .shouldBeInstanceOf<PollClosedEvent>()

        event.cid shouldBeEqualTo "messaging:general"
        event.channelType shouldBeEqualTo "messaging"
        event.channelId shouldBeEqualTo "general"
        event.messageId shouldBeEqualTo "message-id"
    }

    @Test
    fun `A vote event carries the vote, and an answer carries the answer`() {
        parser.fromJson(pollEventJson("poll.vote_casted", createPollVoteJsonString()), ChatEvent::class.java)
            .shouldBeInstanceOf<VoteCastedEvent>().newVote.id shouldBeEqualTo "vote-id"
        parser.fromJson(pollEventJson("poll.vote_casted", createPollAnswerVoteJsonString()), ChatEvent::class.java)
            .shouldBeInstanceOf<AnswerCastedEvent>().newAnswer.text shouldBeEqualTo "My answer"
        parser.fromJson(pollEventJson("poll.vote_removed", createPollVoteJsonString()), ChatEvent::class.java)
            .shouldBeInstanceOf<VoteRemovedEvent>().removedVote.id shouldBeEqualTo "vote-id"
    }

    @Test
    fun `A synced poll event keeps the created_at string as sent`() {
        val response = parser.fromJson(
            """{ "events": [${pollEventJson("poll.closed")}] }""",
            SyncHistoryResponse::class.java,
        )

        val event = with(eventMapping) { response.events.single().toDomain() }

        event.rawCreatedAt shouldBeEqualTo NANOSECOND_CREATED_AT
    }

    @Test
    fun `A poll event without a cid is rejected like the hand-written DTO rejected it`() {
        assertThrows<JsonDataException> {
            parser.fromJson(pollEventJson("poll.closed", cid = null), ChatEvent::class.java)
        }
    }

    private fun pollEventJson(type: String, vote: String? = null, cid: String? = "messaging:general") = """
        {
          "type": "$type",
          "created_at": "$NANOSECOND_CREATED_AT",
          ${cid?.let { "\"cid\": \"$it\"," }.orEmpty()}
          "message_id": "message-id",
          ${vote?.let { "\"poll_vote\": $it," }.orEmpty()}
          "poll": ${createPollJsonString()}
        }
    """.trimIndent()

    companion object {
        private const val NANOSECOND_CREATED_AT = "2026-09-23T14:57:25.025029486Z"

        // The same instant truncated to milliseconds.
        private const val CREATED_AT_MILLIS = 1790175445025L

        @JvmStatic
        fun events(): List<Arguments> = listOf(
            Arguments.of("poll.closed", null, PollClosedEvent::class),
            Arguments.of("poll.deleted", null, PollDeletedEvent::class),
            Arguments.of("poll.updated", null, PollUpdatedEvent::class),
            Arguments.of("poll.vote_casted", createPollVoteJsonString(), VoteCastedEvent::class),
            Arguments.of("poll.vote_changed", createPollVoteJsonString(), VoteChangedEvent::class),
            Arguments.of("poll.vote_changed", createPollAnswerVoteJsonString(), AnswerCastedEvent::class),
            Arguments.of("poll.vote_removed", createPollVoteJsonString(), VoteRemovedEvent::class),
        )
    }
}
