/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.api2.model.requests

import com.squareup.moshi.JsonClass

/**
 * Used for creating a new poll.
 *
 * @property text the text of the option.
 */
@JsonClass(generateAdapter = true)
internal data class UpstreamOptionDto(
    val text: String,
)

/**
 * Used for creating a new poll.
 *
 * @property name the name of the poll.
 * @property description the description of the poll.
 * @property options the list of options for the poll.
 * @property voting_visibility the visibility of the poll. Accepted values are "public" and "anonymous".
 * @property enforce_unique_vote Indicates whether users can cast multiple votes.
 * @property max_votes_allowed the maximum number of votes allowed per user. min: 1, max: 10
 * @property allow_user_suggested_options Indicates whether users can suggest new options.
 */
@JsonClass(generateAdapter = true)
internal data class PollRequest(
    val name: String,
    val description: String,
    val options: List<UpstreamOptionDto>,
    val voting_visibility: String,
    val enforce_unique_vote: Boolean,
    val max_votes_allowed: Int,
    val allow_user_suggested_options: Boolean,
) {

    companion object {
        const val VOTING_VISIBILITY_PUBLIC = "public"
        const val VOTING_VISIBILITY_ANONYMOUS = "anonymous"
    }
}

/**
 * Used for suggesting a new option for a poll.
 *
 * @property poll_option the option object.
 */
@JsonClass(generateAdapter = true)
internal data class SuggestPollOptionRequest(
    val text: String,
)

/**
 * Used for voting on a poll.
 *
 * @property vote the vote object.
 */
@JsonClass(generateAdapter = true)
internal data class PollVoteRequest(
    val vote: UpstreamVoteDto,
)

/**
 * Used for updating a poll.
 *
 * @property set the fields to set.
 * @property unset the fields to unset.
 */
@JsonClass(generateAdapter = true)
internal data class PollUpdateRequest(
    val set: Map<String, Any> = emptyMap(),
    val unset: Set<String> = emptySet(),
)

/**
 * Used for voting on a poll.
 *
 * @property option_id the text of the answer.
 */
@JsonClass(generateAdapter = true)
internal data class UpstreamVoteDto(
    val option_id: String,
)
