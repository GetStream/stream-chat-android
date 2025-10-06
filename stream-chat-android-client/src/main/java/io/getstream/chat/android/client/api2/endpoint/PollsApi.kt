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

package io.getstream.chat.android.client.api2.endpoint

import io.getstream.chat.android.client.api.AuthenticatedApi
import io.getstream.chat.android.client.api2.model.requests.CreatePollRequest
import io.getstream.chat.android.client.api2.model.requests.PollUpdateRequest
import io.getstream.chat.android.client.api2.model.requests.PollVoteRequest
import io.getstream.chat.android.client.api2.model.requests.SuggestPollOptionRequest
import io.getstream.chat.android.client.api2.model.response.CompletableResponse
import io.getstream.chat.android.client.api2.model.response.PollResponse
import io.getstream.chat.android.client.api2.model.response.PollVoteResponse
import io.getstream.chat.android.client.api2.model.response.SuggestPollOptionResponse
import io.getstream.chat.android.client.call.RetrofitCall
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

@AuthenticatedApi
internal interface PollsApi {

    /**
     * Creates a new poll.
     *
     * @param body Body holding the poll properties to be created.
     *
     * @return The poll response.
     */
    @POST("/polls")
    fun createPoll(@Body body: CreatePollRequest): RetrofitCall<PollResponse>

    /**
     * Retrieves a poll by its ID.
     *
     * @param pollId The poll ID.
     *
     * @return The poll response.
     */
    @GET("/polls/{poll_id}")
    fun getPoll(@Path("poll_id") pollId: String): RetrofitCall<PollResponse>

    /**
     * Suggest a new option for a poll.
     *
     * @param pollId The poll ID.
     * @param suggestPollOptionRequest The suggest poll option request.
     *
     * @return The suggest poll option response.
     */
    @POST("/polls/{poll_id}/options")
    fun suggestPollOption(
        @Path("poll_id") pollId: String,
        @Body suggestPollOptionRequest: SuggestPollOptionRequest,
    ): RetrofitCall<SuggestPollOptionResponse>

    /**
     * Casts a vote on a poll.
     *
     * @param messageId The message ID.
     * @param pollId The poll ID.
     * @param pollVoteRequest The poll vote request.
     *
     * @return The poll vote response.
     */
    @POST("/messages/{message_id}/polls/{poll_id}/vote")
    fun castPollVote(
        @Path("message_id") messageId: String,
        @Path("poll_id") pollId: String,
        @Body pollVoteRequest: PollVoteRequest,
    ): RetrofitCall<PollVoteResponse>

    /**
     * Removes a vote on a poll.
     *
     * @param messageId The message ID.
     * @param pollId The poll ID.
     * @param voteId The vote ID.
     *
     * @return The poll vote response.
     */
    @DELETE("/messages/{message_id}/polls/{poll_id}/vote/{vote_id}")
    fun removePollVote(
        @Path("message_id") messageId: String,
        @Path("poll_id") pollId: String,
        @Path("vote_id") voteId: String,
    ): RetrofitCall<PollVoteResponse>

    /**
     * Updates a poll.
     *
     * @param pollId The poll ID.
     * @param pollUpdateRequest The poll update request.
     *
     * @return The poll response.
     */
    @PATCH("/polls/{poll_id}")
    fun updatePoll(
        @Path("poll_id") pollId: String,
        @Body pollUpdateRequest: PollUpdateRequest,
    ): RetrofitCall<PollResponse>

    /**
     * Deletes a poll.
     *
     * @param pollId The ID of the poll to delete.
     */
    @DELETE("/polls/{poll_id}")
    fun deletePoll(@Path("poll_id") pollId: String): RetrofitCall<CompletableResponse>
}
