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

package io.getstream.chat.android.offline.repository.domain.message.internal

import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
@Suppress("LongParameterList")
internal class PollEntity(
    val id: String,
    val name: String,
    val description: String,
    val options: List<OptionEntity>,
    val votingVisibility: String,
    val enforceUniqueVote: Boolean,
    val maxVotesAllowed: Int,
    val allowUserSuggestedOptions: Boolean,
    val allowAnswers: Boolean,
    val voteCountsByOption: Map<String, Int>,
    val votes: List<VoteEntity>,
    val ownVotes: List<VoteEntity>,
    val createdAt: Date,
    val updatedAt: Date,
    val closed: Boolean,
)

@JsonClass(generateAdapter = true)
internal class OptionEntity(
    val id: String,
    val text: String,
)

@JsonClass(generateAdapter = true)
internal class VoteEntity(
    val id: String,
    val pollId: String,
    val optionId: String,
    val createdAt: Date,
    val updatedAt: Date,
    val userId: String?,
)
