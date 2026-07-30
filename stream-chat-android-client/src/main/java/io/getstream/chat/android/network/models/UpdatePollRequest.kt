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

/**
 *
 */
@com.squareup.moshi.JsonClass(generateAdapter = true)
internal data class UpdatePollRequest(
    @Json(name = "id")
    internal val id: String,

    @Json(name = "name")
    internal val name: String,

    @Json(name = "allow_answers")
    internal val allowAnswers: Boolean? = null,

    @Json(name = "allow_user_suggested_options")
    internal val allowUserSuggestedOptions: Boolean? = null,

    @Json(name = "description")
    internal val description: String? = null,

    @Json(name = "enforce_unique_vote")
    internal val enforceUniqueVote: Boolean? = null,

    @Json(name = "is_closed")
    internal val isClosed: Boolean? = null,

    @Json(name = "max_votes_allowed")
    internal val maxVotesAllowed: Int? = null,

    @Json(name = "voting_visibility")
    internal val votingVisibility: VotingVisibility? = null,

    @Json(name = "options")
    internal val options: List<io.getstream.chat.android.network.models.PollOptionRequest>? = emptyList(),

    @Json(name = "custom")
    internal val custom: Map<String, Any?>? = emptyMap(),
) {

    /**
     * VotingVisibility Enum
     */
    internal sealed class VotingVisibility(internal val value: String) {
        override fun toString(): String = value

        internal companion object {
            internal fun fromString(s: String): VotingVisibility = when (s) {
                "anonymous" -> Anonymous
                "public" -> Public
                else -> Unknown(s)
            }
        }
        internal object Anonymous : VotingVisibility("anonymous")
        internal object Public : VotingVisibility("public")
        internal data class Unknown(val unknownValue: String) : VotingVisibility(unknownValue)

        internal class VotingVisibilityAdapter : JsonAdapter<VotingVisibility>() {
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
