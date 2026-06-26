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

import io.getstream.chat.android.client.api2.model.dto.DownstreamPollDto
import io.getstream.chat.android.network.models.PollOptionResponseData as DownstreamPollOptionDto
import io.getstream.chat.android.client.api2.model.requests.CreatePollRequest
import io.getstream.chat.android.client.api2.model.requests.UpstreamOptionDto
import org.intellij.lang.annotations.Language
import java.util.Date

internal object PollDtoTestData {

    // DownstreamPollOptionDto Test Data

    @Language("JSON")
    val downstreamPollOptionJson =
        """{
          "id": "option1",
          "text": "Option 1",
          "extraData": {
            "key1": "value1",
            "key2": true
          },
          "customKey": "customValue"
        }"""

    val downstreamPollOption = DownstreamPollOptionDto(
        id = "option1",
        text = "Option 1",
        custom = mapOf(
            "extraData" to mapOf(
                "key1" to "value1",
                "key2" to true,
            ),
            "customKey" to "customValue",
        ),
    )

    @Language("JSON")
    val downstreamPollOptionJsonWithoutExtraData =
        """{
          "id": "option2",
          "text": "Option 2"
        }"""

    val downstreamPollOptionWithoutExtraData = DownstreamPollOptionDto(
        id = "option2",
        text = "Option 2",
        custom = emptyMap(),
    )

    // DownstreamPollDto Test Data

    @Language("JSON")
    val downstreamPollJson =
        """{
          "allow_answers": true,
          "allow_user_suggested_options": false,
          "answers_count": 5,
          "created_at": "2020-06-10T11:04:31.000Z",
          "created_by": ${UserDtoTestData.downstreamJson},
          "created_by_id": "userId",
          "description": "Poll description",
          "enforce_unique_vote": true,
          "id": "poll1",
          "is_closed": false,
          "latest_answers": [],
          "latest_votes_by_option": {},
          "max_votes_allowed": 1,
          "name": "Poll Name",
          "options": [
            {
              "id": "option1",
              "text": "Option 1"
            }
          ],
          "own_votes": [],
          "updated_at": "2020-06-10T11:04:31.588Z",
          "vote_count": 10,
          "vote_counts_by_option": {
            "option1": 10
          },
          "voting_visibility": "public",
          "poll_key": "poll_value",
          "customPollKey": "customPollValue"
        }"""

    val downstreamPoll = DownstreamPollDto(
        allowAnswers = true,
        allowUserSuggestedOptions = false,
        answersCount = 5,
        createdAt = Date(1591787071000),
        createdBy = UserDtoTestData.downstreamUser,
        createdById = "userId",
        description = "Poll description",
        enforceUniqueVote = true,
        id = "poll1",
        isClosed = false,
        latestAnswers = emptyList(),
        latestVotesByOption = emptyMap(),
        maxVotesAllowed = 1,
        name = "Poll Name",
        options = listOf(
            DownstreamPollOptionDto(
                id = "option1",
                text = "Option 1",
                custom = emptyMap(),
            ),
        ),
        ownVotes = emptyList(),
        updatedAt = Date(1591787071588),
        voteCount = 10,
        voteCountsByOption = mapOf("option1" to 10),
        votingVisibility = "public",
        custom = mapOf(
            "poll_key" to "poll_value",
            "customPollKey" to "customPollValue",
        ),
    )

    @Language("JSON")
    val downstreamPollJsonWithoutExtraData =
        """{
          "allow_answers": false,
          "allow_user_suggested_options": true,
          "answers_count": 0,
          "created_at": "2020-06-10T11:04:31.000Z",
          "created_by": ${UserDtoTestData.downstreamJson},
          "created_by_id": "userId",
          "description": "Simple poll",
          "enforce_unique_vote": false,
          "id": "poll2",
          "name": "Simple Poll",
          "options": [],
          "own_votes": [],
          "updated_at": "2020-06-10T11:04:31.588Z",
          "vote_count": 0,
          "voting_visibility": "public"
        }"""

    val downstreamPollWithoutExtraData = DownstreamPollDto(
        allowAnswers = false,
        allowUserSuggestedOptions = true,
        answersCount = 0,
        createdAt = Date(1591787071000),
        createdBy = UserDtoTestData.downstreamUser,
        createdById = "userId",
        description = "Simple poll",
        enforceUniqueVote = false,
        id = "poll2",
        name = "Simple Poll",
        options = emptyList(),
        ownVotes = emptyList(),
        updatedAt = Date(1591787071588),
        voteCount = 0,
        votingVisibility = "public",
        custom = emptyMap(),
    )

    // UpstreamOptionDto Test Data

    @Language("JSON")
    val upstreamOptionJson =
        """{
          "text": "option",
          "customKey1": "customValue1",
          "customKey2": 42.0
        }""".withoutWhitespace()

    val upstreamOption = UpstreamOptionDto(
        text = "option",
        extraData = mapOf(
            "customKey1" to "customValue1",
            "customKey2" to 42.0, // JSON numbers are parsed as Double
        ),
    )

    @Language("JSON")
    val upstreamOptionJsonWithoutExtraData =
        """{
          "text": "option"
        }""".withoutWhitespace()

    val upstreamOptionWithoutExtraData = UpstreamOptionDto(
        text = "option",
        extraData = emptyMap(),
    )

    // CreatePollRequest Test Data

    @Language("JSON")
    val createPollRequestJson =
        """{
          "allow_answers": true,
          "allow_user_suggested_options": false,
          "description": "description",
          "enforce_unique_vote": true,
          "max_votes_allowed": 1,
          "name": "poll",
          "options": [
            {
              "text": "option"
            }
          ],
          "voting_visibility": "public",
          "customRequestKey": "customRequestValue"
        }""".withoutWhitespace()

    val createPollRequest = CreatePollRequest(
        allow_answers = true,
        allow_user_suggested_options = false,
        description = "description",
        enforce_unique_vote = true,
        max_votes_allowed = 1,
        name = "poll",
        options = listOf(
            UpstreamOptionDto(
                text = "option",
                extraData = emptyMap(),
            ),
        ),
        voting_visibility = "public",
        extraData = mapOf(
            "customRequestKey" to "customRequestValue",
        ),
    )

    @Language("JSON")
    val createPollRequestJsonWithoutExtraData =
        """{
          "allow_answers": false,
          "allow_user_suggested_options": false,
          "description": "",
          "enforce_unique_vote": false,
          "max_votes_allowed": 1,
          "name": "poll",
          "options": [],
          "voting_visibility": "public"
        }""".withoutWhitespace()

    val createPollRequestWithoutExtraData = CreatePollRequest(
        allow_answers = false,
        allow_user_suggested_options = false,
        description = "",
        enforce_unique_vote = false,
        max_votes_allowed = 1,
        name = "poll",
        options = emptyList(),
        voting_visibility = "public",
        extraData = emptyMap(),
    )
}
