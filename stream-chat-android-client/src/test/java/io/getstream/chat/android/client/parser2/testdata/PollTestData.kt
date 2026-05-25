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

package io.getstream.chat.android.client.parser2.testdata

import io.getstream.chat.android.models.Answer
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.models.VotingVisibility
import org.intellij.lang.annotations.Language
import java.util.Date

internal object PollTestData {

    @Language("JSON")
    val jsonAllFields =
        """{"id":"poll1","name":"Favorite color","description":"Choose your favorite color","options":[{"id":"option1","text":"Red"},{"id":"option2","text":"Blue"}],"voting_visibility":"public","enforce_unique_vote":true,"max_votes_allowed":1,"allow_user_suggested_options":false,"allow_answers":true,"vote_count":2,"vote_counts_by_option":{"option1":1,"option2":1},"latest_votes_by_option":{"option1":[{"id":"vote1","poll_id":"poll1","option_id":"option1","created_at":"2024-01-01T10:00:00.000Z","updated_at":"2024-01-01T11:00:00.000Z","user":{"id":"user1","role":"user","banned":false,"online":true},"is_answer":false}],"option2":[{"id":"vote2","poll_id":"poll1","option_id":"option2","created_at":"2024-01-01T10:00:00.000Z","updated_at":"2024-01-01T11:00:00.000Z","user":{"id":"user2","role":"user","banned":false,"online":true},"is_answer":false}]},"own_votes":[{"id":"vote1","poll_id":"poll1","option_id":"option1","created_at":"2024-01-01T10:00:00.000Z","updated_at":"2024-01-01T11:00:00.000Z","user":{"id":"user1","role":"user","banned":false,"online":true},"is_answer":false}],"created_at":"2024-01-01T09:00:00.000Z","updated_at":"2024-01-01T12:00:00.000Z","is_closed":false,"answers_count":1,"latest_answers":[{"id":"answer1","poll_id":"poll1","option_id":"","answer_text":"Purple","created_at":"2024-01-01T10:00:00.000Z","updated_at":"2024-01-01T11:00:00.000Z","user":{"id":"user3","role":"user","banned":false,"online":true},"is_answer":true}],"created_by":{"id":"admin","role":"admin","banned":false,"online":true},"created_by_id":"admin"}"""

    @Language("JSON")
    val jsonOptionalFieldsMissing =
        """{"id":"poll1","name":"Favorite color","description":"Choose your favorite color","options":[{"id":"option1","text":"Red"}],"enforce_unique_vote":true,"allow_user_suggested_options":false,"allow_answers":false,"vote_count":0,"own_votes":[],"created_at":"2024-01-01T09:00:00.000Z","updated_at":"2024-01-01T12:00:00.000Z","answers_count":0,"created_by_id":"admin"}"""

    @Language("JSON")
    val jsonMissingId =
        """{"name":"Favorite color","description":"Choose your favorite color","options":[],"enforce_unique_vote":true,"allow_user_suggested_options":false,"allow_answers":false,"vote_count":0,"own_votes":[],"created_at":"2024-01-01T09:00:00.000Z","updated_at":"2024-01-01T12:00:00.000Z","answers_count":0,"created_by_id":"admin"}"""

    @Language("JSON")
    val jsonMissingName =
        """{"id":"poll1","description":"Choose your favorite color","options":[],"enforce_unique_vote":true,"allow_user_suggested_options":false,"allow_answers":false,"vote_count":0,"own_votes":[],"created_at":"2024-01-01T09:00:00.000Z","updated_at":"2024-01-01T12:00:00.000Z","answers_count":0,"created_by_id":"admin"}"""

    @Language("JSON")
    val jsonMissingDescription =
        """{"id":"poll1","name":"Favorite color","options":[],"enforce_unique_vote":true,"allow_user_suggested_options":false,"allow_answers":false,"vote_count":0,"own_votes":[],"created_at":"2024-01-01T09:00:00.000Z","updated_at":"2024-01-01T12:00:00.000Z","answers_count":0,"created_by_id":"admin"}"""

    @Language("JSON")
    val jsonMissingOptions =
        """{"id":"poll1","name":"Favorite color","description":"Choose your favorite color","enforce_unique_vote":true,"allow_user_suggested_options":false,"allow_answers":false,"vote_count":0,"own_votes":[],"created_at":"2024-01-01T09:00:00.000Z","updated_at":"2024-01-01T12:00:00.000Z","answers_count":0,"created_by_id":"admin"}"""

    @Language("JSON")
    val jsonMissingEnforceUniqueVote =
        """{"id":"poll1","name":"Favorite color","description":"Choose your favorite color","options":[],"allow_user_suggested_options":false,"allow_answers":false,"vote_count":0,"own_votes":[],"created_at":"2024-01-01T09:00:00.000Z","updated_at":"2024-01-01T12:00:00.000Z","answers_count":0,"created_by_id":"admin"}"""

    @Language("JSON")
    val jsonWithMixedVotesAndAnswers =
        """{"id":"poll1","name":"Favorite color","description":"Choose your favorite color","options":[{"id":"option1","text":"Red"}],"enforce_unique_vote":true,"allow_user_suggested_options":false,"allow_answers":true,"vote_count":2,"vote_counts_by_option":{"option1":2},"latest_votes_by_option":{"option1":[{"id":"vote1","poll_id":"poll1","option_id":"option1","created_at":"2024-01-01T10:00:00.000Z","updated_at":"2024-01-01T11:00:00.000Z","user":{"id":"user1","role":"user","banned":false,"online":true},"is_answer":false},{"id":"answer1","poll_id":"poll1","option_id":"","answer_text":"Purple","created_at":"2024-01-01T10:00:00.000Z","updated_at":"2024-01-01T11:00:00.000Z","user":{"id":"user2","role":"user","banned":false,"online":true},"is_answer":true},{"id":"vote2","poll_id":"poll1","option_id":"option1","created_at":"2024-01-01T10:00:00.000Z","updated_at":"2024-01-01T11:00:00.000Z","user":{"id":"user3","role":"user","banned":false,"online":true},"is_answer":false}]},"own_votes":[{"id":"vote1","poll_id":"poll1","option_id":"option1","created_at":"2024-01-01T10:00:00.000Z","updated_at":"2024-01-01T11:00:00.000Z","user":{"id":"user1","role":"user","banned":false,"online":true},"is_answer":false},{"id":"answer2","poll_id":"poll1","option_id":"","answer_text":"Green","created_at":"2024-01-01T10:00:00.000Z","updated_at":"2024-01-01T11:00:00.000Z","user":{"id":"user1","role":"user","banned":false,"online":true},"is_answer":true}],"created_at":"2024-01-01T09:00:00.000Z","updated_at":"2024-01-01T12:00:00.000Z","answers_count":2,"latest_answers":[{"id":"answer1","poll_id":"poll1","option_id":"","answer_text":"Purple","created_at":"2024-01-01T10:00:00.000Z","updated_at":"2024-01-01T11:00:00.000Z","user":{"id":"user2","role":"user","banned":false,"online":true},"is_answer":true},{"id":"answer2","poll_id":"poll1","option_id":"","answer_text":"Green","created_at":"2024-01-01T10:00:00.000Z","updated_at":"2024-01-01T11:00:00.000Z","user":{"id":"user1","role":"user","banned":false,"online":true},"is_answer":true}],"created_by_id":"admin"}"""

    @Language("JSON")
    val jsonWithExtraData =
        """{"id":"poll1","name":"Favorite color","description":"Choose your favorite color","options":[{"id":"option1","text":"Red"}],"enforce_unique_vote":true,"allow_user_suggested_options":false,"allow_answers":false,"vote_count":0,"own_votes":[],"created_at":"2024-01-01T09:00:00.000Z","updated_at":"2024-01-01T12:00:00.000Z","answers_count":0,"created_by_id":"admin","custom_field":"custom_value","custom_number":42}"""

    val expectedAllFields = Poll(
        id = "poll1",
        name = "Favorite color",
        description = "Choose your favorite color",
        options = listOf(
            Option(id = "option1", text = "Red"),
            Option(id = "option2", text = "Blue"),
        ),
        votingVisibility = VotingVisibility.PUBLIC,
        enforceUniqueVote = true,
        maxVotesAllowed = 1,
        allowUserSuggestedOptions = false,
        allowAnswers = true,
        voteCount = 2,
        voteCountsByOption = mapOf("option1" to 1, "option2" to 1),
        votes = listOf(
            Vote(
                id = "vote1",
                pollId = "poll1",
                optionId = "option1",
                createdAt = Date(1704103200000L),
                updatedAt = Date(1704106800000L),
                user = User(id = "user1", role = "user", invisible = false, banned = false, online = true),
            ),
            Vote(
                id = "vote2",
                pollId = "poll1",
                optionId = "option2",
                createdAt = Date(1704103200000L),
                updatedAt = Date(1704106800000L),
                user = User(id = "user2", role = "user", invisible = false, banned = false, online = true),
            ),
        ),
        ownVotes = listOf(
            Vote(
                id = "vote1",
                pollId = "poll1",
                optionId = "option1",
                createdAt = Date(1704103200000L),
                updatedAt = Date(1704106800000L),
                user = User(id = "user1", role = "user", invisible = false, banned = false, online = true),
            ),
        ),
        createdAt = Date(1704099600000L),
        updatedAt = Date(1704110400000L),
        closed = false,
        answersCount = 1,
        answers = listOf(
            Answer(
                id = "answer1",
                pollId = "poll1",
                text = "Purple",
                createdAt = Date(1704103200000L),
                updatedAt = Date(1704106800000L),
                user = User(id = "user3", role = "user", invisible = false, banned = false, online = true),
            ),
        ),
        createdBy = User(id = "admin", role = "admin", invisible = false, banned = false, online = true),
        extraData = emptyMap(),
    )

    val expectedOptionalFieldsMissing = Poll(
        id = "poll1",
        name = "Favorite color",
        description = "Choose your favorite color",
        options = listOf(Option(id = "option1", text = "Red")),
        votingVisibility = VotingVisibility.PUBLIC,
        enforceUniqueVote = true,
        maxVotesAllowed = null,
        allowUserSuggestedOptions = false,
        allowAnswers = false,
        voteCount = 0,
        voteCountsByOption = emptyMap(),
        votes = emptyList(),
        ownVotes = emptyList(),
        createdAt = Date(1704099600000L),
        updatedAt = Date(1704110400000L),
        closed = false,
        answersCount = 0,
        answers = emptyList(),
        createdBy = null,
        extraData = emptyMap(),
    )

    val expectedMixedVotesAndAnswers = Poll(
        id = "poll1",
        name = "Favorite color",
        description = "Choose your favorite color",
        options = listOf(Option(id = "option1", text = "Red")),
        votingVisibility = VotingVisibility.PUBLIC,
        enforceUniqueVote = true,
        maxVotesAllowed = null,
        allowUserSuggestedOptions = false,
        allowAnswers = true,
        voteCount = 2,
        voteCountsByOption = mapOf("option1" to 2),
        votes = listOf(
            Vote(
                id = "vote1",
                pollId = "poll1",
                optionId = "option1",
                createdAt = Date(1704103200000L),
                updatedAt = Date(1704106800000L),
                user = User(id = "user1", role = "user", invisible = false, banned = false, online = true),
            ),
            Vote(
                id = "vote2",
                pollId = "poll1",
                optionId = "option1",
                createdAt = Date(1704103200000L),
                updatedAt = Date(1704106800000L),
                user = User(id = "user3", role = "user", invisible = false, banned = false, online = true),
            ),
        ),
        ownVotes = listOf(
            Vote(
                id = "vote1",
                pollId = "poll1",
                optionId = "option1",
                createdAt = Date(1704103200000L),
                updatedAt = Date(1704106800000L),
                user = User(id = "user1", role = "user", invisible = false, banned = false, online = true),
            ),
        ),
        createdAt = Date(1704099600000L),
        updatedAt = Date(1704110400000L),
        closed = false,
        answersCount = 2,
        answers = listOf(
            Answer(
                id = "answer1",
                pollId = "poll1",
                text = "Purple",
                createdAt = Date(1704103200000L),
                updatedAt = Date(1704106800000L),
                user = User(id = "user2", role = "user", invisible = false, banned = false, online = true),
            ),
            Answer(
                id = "answer2",
                pollId = "poll1",
                text = "Green",
                createdAt = Date(1704103200000L),
                updatedAt = Date(1704106800000L),
                user = User(id = "user1", role = "user", invisible = false, banned = false, online = true),
            ),
        ),
        createdBy = null,
        extraData = emptyMap(),
    )

    val expectedWithExtraData = Poll(
        id = "poll1",
        name = "Favorite color",
        description = "Choose your favorite color",
        options = listOf(Option(id = "option1", text = "Red")),
        votingVisibility = VotingVisibility.PUBLIC,
        enforceUniqueVote = true,
        maxVotesAllowed = null,
        allowUserSuggestedOptions = false,
        allowAnswers = false,
        voteCount = 0,
        voteCountsByOption = emptyMap(),
        votes = emptyList(),
        ownVotes = emptyList(),
        createdAt = Date(1704099600000L),
        updatedAt = Date(1704110400000L),
        closed = false,
        answersCount = 0,
        answers = emptyList(),
        createdBy = null,
        extraData = mapOf("custom_field" to "custom_value", "custom_number" to 42.0),
    )

    @Language("JSON")
    val jsonWithExplicitNulls =
        """{"id":"poll1","name":"Favorite color","description":"Choose your favorite color","options":[{"id":"option1","text":"Red"}],"enforce_unique_vote":true,"allow_user_suggested_options":false,"allow_answers":false,"vote_count":0,"own_votes":[],"created_at":"2024-01-01T09:00:00.000Z","updated_at":"2024-01-01T12:00:00.000Z","answers_count":0,"created_by_id":"admin","voting_visibility":null,"max_votes_allowed":null,"is_closed":null}"""

    val expectedWithExplicitNulls = Poll(
        id = "poll1",
        name = "Favorite color",
        description = "Choose your favorite color",
        options = listOf(Option(id = "option1", text = "Red")),
        votingVisibility = VotingVisibility.PUBLIC,
        enforceUniqueVote = true,
        maxVotesAllowed = null,
        allowUserSuggestedOptions = false,
        allowAnswers = false,
        voteCount = 0,
        voteCountsByOption = emptyMap(),
        votes = emptyList(),
        ownVotes = emptyList(),
        createdAt = Date(1704099600000L),
        updatedAt = Date(1704110400000L),
        closed = false,
        answersCount = 0,
        answers = emptyList(),
        createdBy = null,
        extraData = emptyMap(),
    )

    // region Malformed vote test data

    /** Poll JSON where a vote in own_votes is missing the required "id" field. */
    @Language("JSON")
    val jsonWithMalformedVoteMissingId =
        """{"id":"poll1","name":"Favorite color","description":"Choose your favorite color","options":[{"id":"option1","text":"Red"}],"enforce_unique_vote":true,"allow_user_suggested_options":false,"allow_answers":false,"vote_count":1,"own_votes":[{"poll_id":"poll1","option_id":"option1","created_at":"2024-01-01T10:00:00.000Z","updated_at":"2024-01-01T11:00:00.000Z","user":{"id":"user1","role":"user","banned":false,"online":true},"is_answer":false}],"created_at":"2024-01-01T09:00:00.000Z","updated_at":"2024-01-01T12:00:00.000Z","answers_count":0,"created_by_id":"admin"}"""

    // endregion
}
