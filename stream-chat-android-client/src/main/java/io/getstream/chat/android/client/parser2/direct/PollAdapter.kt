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

package io.getstream.chat.android.client.parser2.direct

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import io.getstream.chat.android.models.Answer
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.models.VotingVisibility
import java.util.Date

internal class PollAdapter(
    private val userAdapter: JsonAdapter<User>,
    private val optionAdapter: JsonAdapter<Option>,
    private val dateAdapter: JsonAdapter<Date>,
    private val currentUserIdProvider: () -> String?,
) : JsonAdapter<Poll>() {

    private data class ParsedVoteDto(
        val id: String,
        val pollId: String,
        val optionId: String,
        val createdAt: Date,
        val updatedAt: Date,
        val user: User?,
        val isAnswer: Boolean,
        val answerText: String?,
    )

    @Suppress("LongMethod")
    override fun fromJson(reader: JsonReader): Poll? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()

        reader.beginObject()

        var id: String? = null
        var name: String? = null
        var description: String? = null
        var options: List<Option>? = null
        var votingVisibility: String? = null
        var enforceUniqueVote: Boolean? = null
        var maxVotesAllowed: Int? = null
        var allowUserSuggestedOptions: Boolean? = null
        var allowAnswers: Boolean? = null
        var voteCount: Int? = null
        var voteCountsByOption: Map<String, Int>? = null
        var latestVotesByOption: Map<String, List<ParsedVoteDto>>? = null
        var ownVotes: List<ParsedVoteDto>? = null
        var createdAt: Date? = null
        var updatedAt: Date? = null
        var closed: Boolean? = null
        var answersCount: Int? = null
        var answers: List<ParsedVoteDto>? = null
        var createdBy: User? = null
        var extraData: MutableMap<String, Any>? = null

        while (reader.hasNext()) {
            val key = reader.nextName()
            when (key) {
                "id" -> id = reader.nextString()
                "name" -> name = reader.nextString()
                "description" -> description = reader.nextString()
                "options" -> options = JsonParsingUtils.parseList(reader, optionAdapter)
                "voting_visibility" -> votingVisibility = JsonParsingUtils.readNullableString(reader)
                "enforce_unique_vote" -> enforceUniqueVote = reader.nextBoolean()
                "max_votes_allowed" -> maxVotesAllowed = JsonParsingUtils.readNullableInt(reader)
                "allow_user_suggested_options" -> allowUserSuggestedOptions = reader.nextBoolean()
                "allow_answers" -> allowAnswers = reader.nextBoolean()
                "vote_count" -> voteCount = reader.nextInt()
                "vote_counts_by_option" -> voteCountsByOption = JsonParsingUtils.parseIntMap(reader)
                "latest_votes_by_option" -> latestVotesByOption = parseParsedVotesMap(reader)
                "own_votes" -> ownVotes = parseParsedVotesList(reader)
                "created_at" -> createdAt = dateAdapter.fromJson(reader)
                "updated_at" -> updatedAt = dateAdapter.fromJson(reader)
                "is_closed" -> closed = JsonParsingUtils.readNullableBoolean(reader)
                "answers_count" -> answersCount = reader.nextInt()
                "latest_answers" -> answers = parseParsedVotesList(reader)
                "created_by" -> createdBy = userAdapter.fromJson(reader)
                "created_by_id" -> reader.skipValue()
                else -> extraData = JsonParsingUtils.accumulateExtraData(key, reader, extraData)
            }
        }
        reader.endObject()

        JsonParsingUtils.requireField(id, "id", reader)
        JsonParsingUtils.requireField(name, "name", reader)
        JsonParsingUtils.requireField(description, "description", reader)
        JsonParsingUtils.requireField(options, "options", reader)
        JsonParsingUtils.requireField(enforceUniqueVote, "enforce_unique_vote", reader)
        JsonParsingUtils.requireField(allowUserSuggestedOptions, "allow_user_suggested_options", reader)
        JsonParsingUtils.requireField(allowAnswers, "allow_answers", reader)
        JsonParsingUtils.requireField(voteCount, "vote_count", reader)
        JsonParsingUtils.requireField(ownVotes, "own_votes", reader)
        JsonParsingUtils.requireField(createdAt, "created_at", reader)
        JsonParsingUtils.requireField(updatedAt, "updated_at", reader)
        JsonParsingUtils.requireField(answersCount, "answers_count", reader)

        val ownUserId = currentUserIdProvider() ?: ownVotes.firstOrNull()?.user?.id

        // Split parsed votes/answers into actual Vote and Answer objects
        val votes = latestVotesByOption
            ?.values
            ?.flatten()
            ?.filter { !it.isAnswer }
            ?.map { it.toVote() } ?: emptyList()
        val mergedOwnVotes = (
            ownVotes
                .filter { !it.isAnswer }
                .map { it.toVote() } +
                votes.filter { it.user?.id == ownUserId }
            )
            .associateBy { it.id }
            .values
            .toList()

        val answersList = answers?.map { it.toAnswer() } ?: emptyList()

        return Poll(
            id = id,
            name = name,
            description = description,
            options = options,
            votingVisibility = toVotingVisibility(votingVisibility),
            enforceUniqueVote = enforceUniqueVote,
            maxVotesAllowed = maxVotesAllowed ?: 1,
            allowUserSuggestedOptions = allowUserSuggestedOptions,
            allowAnswers = allowAnswers,
            voteCount = voteCount,
            voteCountsByOption = voteCountsByOption ?: emptyMap(),
            votes = votes,
            ownVotes = mergedOwnVotes,
            createdAt = createdAt,
            updatedAt = updatedAt,
            closed = closed ?: false,
            answersCount = answersCount,
            answers = answersList,
            createdBy = createdBy,
            extraData = extraData?.toMap() ?: emptyMap(),
        )
    }

    private fun ParsedVoteDto.toVote() = Vote(
        id = id,
        pollId = pollId,
        optionId = optionId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        user = user,
    )

    private fun ParsedVoteDto.toAnswer() = Answer(
        id = id,
        pollId = pollId,
        text = answerText ?: "",
        createdAt = createdAt,
        updatedAt = updatedAt,
        user = user,
    )

    private fun parseParsedVotesList(reader: JsonReader): List<ParsedVoteDto>? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()
        if (reader.peek() != JsonReader.Token.BEGIN_ARRAY) {
            reader.skipValue()
            return null
        }
        reader.beginArray()
        val list = mutableListOf<ParsedVoteDto>()
        while (reader.hasNext()) {
            parseParsedVote(reader)?.let { list.add(it) }
        }
        reader.endArray()
        return list
    }

    private fun parseParsedVote(reader: JsonReader): ParsedVoteDto? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()

        reader.beginObject()
        var id: String? = null
        var pollId: String? = null
        var optionId: String? = null
        var createdAt: Date? = null
        var updatedAt: Date? = null
        var user: User? = null
        var isAnswer: Boolean = false
        var answerText: String? = null

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "id" -> id = reader.nextString()
                "poll_id" -> pollId = reader.nextString()
                "option_id" -> optionId = reader.nextString()
                "created_at" -> createdAt = dateAdapter.fromJson(reader)
                "updated_at" -> updatedAt = dateAdapter.fromJson(reader)
                "user" -> user = userAdapter.fromJson(reader)
                "is_answer" -> isAnswer = JsonParsingUtils.readNullableBoolean(reader) ?: false
                "answer_text" -> answerText = JsonParsingUtils.readNullableString(reader)
                "user_id" -> reader.skipValue()
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        if (id == null) return null
        if (pollId == null) return null
        if (optionId == null) return null
        if (createdAt == null) return null
        if (updatedAt == null) return null

        return ParsedVoteDto(
            id = id,
            pollId = pollId,
            optionId = optionId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            user = user,
            isAnswer = isAnswer,
            answerText = answerText,
        )
    }

    private fun parseParsedVotesMap(reader: JsonReader): Map<String, List<ParsedVoteDto>>? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()
        if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) {
            reader.skipValue()
            return null
        }
        reader.beginObject()
        val map = mutableMapOf<String, List<ParsedVoteDto>>()
        while (reader.hasNext()) {
            val key = reader.nextName()
            parseParsedVotesList(reader)?.let { map[key] = it }
        }
        reader.endObject()
        return map
    }

    private fun toVotingVisibility(value: String?): VotingVisibility = when (value) {
        null, "public" -> VotingVisibility.PUBLIC
        "anonymous" -> VotingVisibility.ANONYMOUS
        else -> throw IllegalArgumentException("Unknown voting visibility: $value")
    }

    override fun toJson(p0: JsonWriter, p1: Poll?) {
        error("Serialization not supported for direct-to-domain path")
    }
}
