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

package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.models.PollOption
import io.getstream.chat.android.models.VotingVisibility

/**
 * Model holding request data for updating a poll.
 *
 * @param id The unique identifier of the poll to be updated.
 * @param name The name of the poll.
 * @param allowAnswers Indicates whether users can suggest user defined answers.
 * @param allowUserSuggestedOptions Indicates whether users can suggest new options.
 * @param description A description of the poll.
 * @param enforceUniqueVote Indicates whether users can cast multiple votes.
 * @param maxVotesAllowed Indicates the maximum amount of votes a user can cast.
 * @param options The list of options for the poll.
 * @param votingVisibility The visibility of the poll.
 * @param extraData A map of additional key-value pairs to include in the creation request.
 */
public data class UpdatePollRequest(
    val id: String,
    val name: String,
    val allowAnswers: Boolean? = null,
    val allowUserSuggestedOptions: Boolean? = null,
    val description: String? = null,
    val enforceUniqueVote: Boolean? = null,
    val isClosed: Boolean? = null,
    val maxVotesAllowed: Int? = null,
    val options: List<PollOption>? = null,
    val votingVisibility: VotingVisibility = VotingVisibility.PUBLIC,
    val extraData: Map<String, Any> = emptyMap(),
)
