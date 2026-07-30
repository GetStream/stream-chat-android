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
import io.getstream.chat.android.client.api2.model.dto.DownstreamPollOptionDto
import io.getstream.chat.android.network.models.CreatePollOptionRequest
import io.getstream.chat.android.network.models.CreatePollRequest
import io.getstream.chat.android.network.models.PollOptionInput
import io.getstream.chat.android.network.models.PollOptionRequest
import io.getstream.chat.android.network.models.UpdatePollOptionRequest
import io.getstream.chat.android.network.models.UpdatePollRequest
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
        extraData = mapOf(
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
        extraData = emptyMap(),
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
          "extraData": {
            "poll_key": "poll_value"
          },
          "customPollKey": "customPollValue"
        }"""

    val downstreamPoll = DownstreamPollDto(
        allow_answers = true,
        allow_user_suggested_options = false,
        answers_count = 5,
        created_at = Date(1591787071000),
        created_by = UserDtoTestData.downstreamUser,
        created_by_id = "userId",
        description = "Poll description",
        enforce_unique_vote = true,
        id = "poll1",
        is_closed = false,
        latest_answers = emptyList(),
        latest_votes_by_option = emptyMap(),
        max_votes_allowed = 1,
        name = "Poll Name",
        options = listOf(
            DownstreamPollOptionDto(
                id = "option1",
                text = "Option 1",
                extraData = emptyMap(),
            ),
        ),
        own_votes = emptyList(),
        updated_at = Date(1591787071588),
        vote_count = 10,
        vote_counts_by_option = mapOf("option1" to 10),
        voting_visibility = "public",
        extraData = mapOf(
            "extraData" to mapOf(
                "poll_key" to "poll_value",
            ),
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
          "is_closed": null,
          "latest_answers": null,
          "latest_votes_by_option": null,
          "max_votes_allowed": null,
          "name": "Simple Poll",
          "options": [],
          "own_votes": [],
          "updated_at": "2020-06-10T11:04:31.588Z",
          "vote_count": 0,
          "vote_counts_by_option": null,
          "voting_visibility": null
        }"""

    val downstreamPollWithoutExtraData = DownstreamPollDto(
        allow_answers = false,
        allow_user_suggested_options = true,
        answers_count = 0,
        created_at = Date(1591787071000),
        created_by = UserDtoTestData.downstreamUser,
        created_by_id = "userId",
        description = "Simple poll",
        enforce_unique_vote = false,
        id = "poll2",
        is_closed = null,
        latest_answers = null,
        latest_votes_by_option = null,
        max_votes_allowed = null,
        name = "Simple Poll",
        options = emptyList(),
        own_votes = emptyList(),
        updated_at = Date(1591787071588),
        vote_count = 0,
        vote_counts_by_option = null,
        voting_visibility = null,
        extraData = emptyMap(),
    )

    // PollOptionInput Test Data

    @Language("JSON")
    val pollOptionInputJson =
        """{
          "text": "option",
          "customKey1": "customValue1",
          "customKey2": 42.0
        }""".withoutWhitespace()

    val pollOptionInput = PollOptionInput(
        text = "option",
        custom = mapOf(
            "customKey1" to "customValue1",
            "customKey2" to 42.0, // JSON numbers are parsed as Double
        ),
    )

    @Language("JSON")
    val pollOptionInputJsonWithoutExtraData =
        """{
          "text": "option"
        }""".withoutWhitespace()

    val pollOptionInputWithoutExtraData = PollOptionInput(
        text = "option",
        custom = emptyMap(),
    )

    // CreatePollRequest Test Data

    @Language("JSON")
    val createPollRequestJson =
        """{
          "name": "poll",
          "allow_answers": true,
          "allow_user_suggested_options": false,
          "description": "description",
          "enforce_unique_vote": true,
          "max_votes_allowed": 1,
          "voting_visibility": "public",
          "options": [
            {
              "text": "option"
            }
          ],
          "customRequestKey": "customRequestValue"
        }""".withoutWhitespace()

    val createPollRequest = CreatePollRequest(
        name = "poll",
        allowAnswers = true,
        allowUserSuggestedOptions = false,
        description = "description",
        enforceUniqueVote = true,
        maxVotesAllowed = 1,
        votingVisibility = CreatePollRequest.VotingVisibility.Public,
        options = listOf(
            PollOptionInput(
                text = "option",
                custom = emptyMap(),
            ),
        ),
        custom = mapOf(
            "customRequestKey" to "customRequestValue",
        ),
    )

    @Language("JSON")
    val createPollRequestJsonWithoutExtraData =
        """{
          "name": "poll",
          "allow_answers": false,
          "allow_user_suggested_options": false,
          "description": "",
          "enforce_unique_vote": false,
          "max_votes_allowed": 1,
          "voting_visibility": "public",
          "options": []
        }""".withoutWhitespace()

    val createPollRequestWithoutExtraData = CreatePollRequest(
        name = "poll",
        allowAnswers = false,
        allowUserSuggestedOptions = false,
        description = "",
        enforceUniqueVote = false,
        maxVotesAllowed = 1,
        votingVisibility = CreatePollRequest.VotingVisibility.Public,
        options = emptyList(),
        custom = emptyMap(),
    )

    // UpdatePollRequest Test Data

    @Language("JSON")
    val updatePollRequestJson =
        """{
          "id": "poll1",
          "name": "poll",
          "voting_visibility": "public",
          "options": [
            {
              "id": "option1",
              "text": "option"
            }
          ],
          "customRequestKey": "customRequestValue"
        }""".withoutWhitespace()

    val updatePollRequest = UpdatePollRequest(
        id = "poll1",
        name = "poll",
        votingVisibility = UpdatePollRequest.VotingVisibility.Public,
        options = listOf(
            PollOptionRequest(
                id = "option1",
                text = "option",
                custom = emptyMap(),
            ),
        ),
        custom = mapOf("customRequestKey" to "customRequestValue"),
    )

    @Language("JSON")
    val updatePollRequestJsonWithoutExtraData =
        """{
          "id": "poll2",
          "name": "poll",
          "voting_visibility": "public",
          "options": []
        }""".withoutWhitespace()

    val updatePollRequestWithoutExtraData = UpdatePollRequest(
        id = "poll2",
        name = "poll",
        votingVisibility = UpdatePollRequest.VotingVisibility.Public,
        options = emptyList(),
        custom = emptyMap(),
    )

    // PollOptionRequest Test Data

    @Language("JSON")
    val pollOptionRequestJson =
        """{
          "id": "option1",
          "text": "option",
          "customKey": "customValue"
        }""".withoutWhitespace()

    val pollOptionRequest = PollOptionRequest(
        id = "option1",
        text = "option",
        custom = mapOf("customKey" to "customValue"),
    )

    @Language("JSON")
    val pollOptionRequestJsonWithoutExtraData =
        """{
          "id": "option2",
          "text": "option"
        }""".withoutWhitespace()

    val pollOptionRequestWithoutExtraData = PollOptionRequest(
        id = "option2",
        text = "option",
        custom = emptyMap(),
    )

    // CreatePollOptionRequest Test Data

    @Language("JSON")
    val createPollOptionRequestJson =
        """{
          "text": "option",
          "customKey": "customValue"
        }""".withoutWhitespace()

    val createPollOptionRequest = CreatePollOptionRequest(
        text = "option",
        custom = mapOf("customKey" to "customValue"),
    )

    @Language("JSON")
    val createPollOptionRequestJsonWithoutExtraData =
        """{
          "text": "option"
        }""".withoutWhitespace()

    val createPollOptionRequestWithoutExtraData = CreatePollOptionRequest(
        text = "option",
        custom = emptyMap(),
    )

    // UpdatePollOptionRequest Test Data

    @Language("JSON")
    val updatePollOptionRequestJson =
        """{
          "id": "option1",
          "text": "option",
          "customKey": "customValue"
        }""".withoutWhitespace()

    val updatePollOptionRequest = UpdatePollOptionRequest(
        id = "option1",
        text = "option",
        custom = mapOf("customKey" to "customValue"),
    )

    @Language("JSON")
    val updatePollOptionRequestJsonWithoutExtraData =
        """{
          "id": "option2",
          "text": "option"
        }""".withoutWhitespace()

    val updatePollOptionRequestWithoutExtraData = UpdatePollOptionRequest(
        id = "option2",
        text = "option",
        custom = emptyMap(),
    )
}
