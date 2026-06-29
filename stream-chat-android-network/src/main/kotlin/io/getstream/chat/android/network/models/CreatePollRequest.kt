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

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport",
)

package io.getstream.chat.android.network.models

import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import kotlin.collections.List
import kotlin.collections.Map

/**
 * Contains all information needed to create a new poll
 */

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class CreatePollRequest(
    @Json(name = "name")
    val name: kotlin.String,

    @Json(name = "allow_answers")
    val allowAnswers: kotlin.Boolean? = null,

    @Json(name = "allow_user_suggested_options")
    val allowUserSuggestedOptions: kotlin.Boolean? = null,

    @Json(name = "description")
    val description: kotlin.String? = null,

    @Json(name = "enforce_unique_vote")
    val enforceUniqueVote: kotlin.Boolean? = null,

    @Json(name = "id")
    val id: kotlin.String? = null,

    @Json(name = "is_closed")
    val isClosed: kotlin.Boolean? = null,

    @Json(name = "max_votes_allowed")
    val maxVotesAllowed: kotlin.Int? = null,

    @Json(name = "voting_visibility")
    val votingVisibility: VotingVisibility? = null,

    @Json(name = "options")
    val options: kotlin.collections.List<io.getstream.chat.android.network.models.PollOptionInput>? = emptyList(),

    @Json(name = "Custom")
    val custom: kotlin.collections.Map<kotlin.String, Any?>? = emptyMap(),
) {

    /**
     * VotingVisibility Enum
     */
    sealed class VotingVisibility(val value: kotlin.String) {
        override fun toString(): String = value

        companion object {
            fun fromString(s: kotlin.String): VotingVisibility = when (s) {
                "anonymous" -> Anonymous
                "public" -> Public
                else -> Unknown(s)
            }
        }
        object Anonymous : VotingVisibility("anonymous")
        object Public : VotingVisibility("public")
        data class Unknown(val unknownValue: kotlin.String) : VotingVisibility(unknownValue)

        class VotingVisibilityAdapter : JsonAdapter<VotingVisibility>() {
            @FromJson
            override fun fromJson(reader: JsonReader): VotingVisibility? {
                val s = reader.nextString() ?: return null
                return VotingVisibility.fromString(s)
            }

            @ToJson
            override fun toJson(writer: JsonWriter, value: VotingVisibility?) {
                writer.value(value?.value)
            }
        }
    }
}
