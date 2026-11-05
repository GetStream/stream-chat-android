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

package io.getstream.chat.android.network.apis

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

internal interface ChatEndpoints {

    /**
     * Cast vote
     * Cast a vote on a poll
     */
    @POST("/api/v2/chat/messages/{message_id}/polls/{poll_id}/vote")
    suspend fun castPollVote(
        @Path("message_id") messageId: String,
        @Path("poll_id") pollId: String,
        @Body castPollVoteRequest: io.getstream.chat.android.network.models.CastPollVoteRequest,
    ): io.getstream.chat.android.network.models.PollVoteResponse

    /**
     * Delete vote
     * Delete a vote from a poll
     */
    @DELETE("/api/v2/chat/messages/{message_id}/polls/{poll_id}/vote/{vote_id}")
    suspend fun deletePollVote(
        @Path("message_id") messageId: String,
        @Path("poll_id") pollId: String,
        @Path("vote_id") voteId: String,
    ): io.getstream.chat.android.network.models.PollVoteResponse

    /**
     * Delete reminder
     * Deletes a user's created reminder
     */
    @DELETE("/api/v2/chat/messages/{message_id}/reminders")
    suspend fun deleteReminder(
        @Path("message_id") messageId: String,
    ): io.getstream.chat.android.network.models.DeleteReminderResponse

    /**
     * Updates Reminder
     * Updates an existing reminder. remind_at and expires_at are both replaced on every call: omitting either one clears it
     */
    @PATCH("/api/v2/chat/messages/{message_id}/reminders")
    suspend fun updateReminder(
        @Path("message_id") messageId: String,
        @Body updateReminderRequest: io.getstream.chat.android.network.models.UpdateReminderRequest,
    ): io.getstream.chat.android.network.models.UpdateReminderResponse

    /**
     * Create reminder
     * Creates a new reminder. An optional expires_at hides the reminder from every read once it passes and stops it counting against the per-user reminder cap
     */
    @POST("/api/v2/chat/messages/{message_id}/reminders")
    suspend fun createReminder(
        @Path("message_id") messageId: String,
        @Body createReminderRequest: io.getstream.chat.android.network.models.CreateReminderRequest,
    ): io.getstream.chat.android.network.models.CreateReminderResponse

    /**
     * Query reminders
     * Queries reminders
     */
    @POST("/api/v2/chat/reminders/query")
    suspend fun queryReminders(
        @Body queryRemindersRequest: io.getstream.chat.android.network.models.QueryRemindersRequest,
    ): io.getstream.chat.android.network.models.QueryRemindersResponse

    /**
     * Create poll
     * Creates a new poll
     */
    @POST("/api/v2/polls")
    suspend fun createPoll(
        @Body createPollRequest: io.getstream.chat.android.network.models.CreatePollRequest,
    ): io.getstream.chat.android.network.models.PollResponse

    /**
     * Update poll
     * Updates a poll
     */
    @PUT("/api/v2/polls")
    suspend fun updatePoll(
        @Body updatePollRequest: io.getstream.chat.android.network.models.UpdatePollRequest,
    ): io.getstream.chat.android.network.models.PollResponse

    /**
     * Query polls
     * Queries polls
     */
    @POST("/api/v2/polls/query")
    suspend fun queryPolls(
        @Body queryPollsRequest: io.getstream.chat.android.network.models.QueryPollsRequest,
    ): io.getstream.chat.android.network.models.QueryPollsResponse

    /**
     * Delete poll
     * Deletes a poll
     */
    @DELETE("/api/v2/polls/{poll_id}")
    suspend fun deletePoll(
        @Path("poll_id") pollId: String,
    ): io.getstream.chat.android.network.models.Response

    /**
     * Get poll
     * Retrieves a poll
     */
    @GET("/api/v2/polls/{poll_id}")
    suspend fun getPoll(
        @Path("poll_id") pollId: String,
    ): io.getstream.chat.android.network.models.PollResponse

    /**
     * Partial update poll
     * Updates a poll partially
     */
    @PATCH("/api/v2/polls/{poll_id}")
    suspend fun updatePollPartial(
        @Path("poll_id") pollId: String,
        @Body updatePollPartialRequest: io.getstream.chat.android.network.models.UpdatePollPartialRequest,
    ): io.getstream.chat.android.network.models.PollResponse

    /**
     * Create poll option
     * Creates a poll option
     */
    @POST("/api/v2/polls/{poll_id}/options")
    suspend fun createPollOption(
        @Path("poll_id") pollId: String,
        @Body createPollOptionRequest: io.getstream.chat.android.network.models.CreatePollOptionRequest,
    ): io.getstream.chat.android.network.models.PollOptionResponse

    /**
     * Update poll option
     * Updates a poll option
     */
    @PUT("/api/v2/polls/{poll_id}/options")
    suspend fun updatePollOption(
        @Path("poll_id") pollId: String,
        @Body updatePollOptionRequest: io.getstream.chat.android.network.models.UpdatePollOptionRequest,
    ): io.getstream.chat.android.network.models.PollOptionResponse

    /**
     * Delete poll option
     * Deletes a poll option
     */
    @DELETE("/api/v2/polls/{poll_id}/options/{option_id}")
    suspend fun deletePollOption(
        @Path("poll_id") pollId: String,
        @Path("option_id") optionId: String,
    ): io.getstream.chat.android.network.models.Response

    /**
     * Query votes
     * Queries votes
     */
    @POST("/api/v2/polls/{poll_id}/votes")
    suspend fun queryPollVotes(
        @Path("poll_id") pollId: String,
        @Body queryPollVotesRequest: io.getstream.chat.android.network.models.QueryPollVotesRequest,
    ): io.getstream.chat.android.network.models.PollVotesResponse
}
