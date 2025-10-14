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
import io.getstream.chat.android.client.api2.model.requests.CreatePollOptionRequest
import io.getstream.chat.android.client.api2.model.requests.CreatePollRequest
import io.getstream.chat.android.client.api2.model.requests.PartialUpdatePollRequest
import io.getstream.chat.android.client.api2.model.requests.PollVoteRequest
import io.getstream.chat.android.client.api2.model.requests.QueryPollsRequest
import io.getstream.chat.android.client.api2.model.requests.UpdatePollOptionRequest
import io.getstream.chat.android.client.api2.model.response.CompletableResponse
import io.getstream.chat.android.client.api2.model.response.PollOptionResponse
import io.getstream.chat.android.client.api2.model.response.PollResponse
import io.getstream.chat.android.client.api2.model.response.PollVoteResponse
import io.getstream.chat.android.client.api2.model.response.QueryPollsResponse
import io.getstream.chat.android.client.call.RetrofitCall
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

@AuthenticatedApi
internal interface PollsApi {

    /**
     * Creates a new poll.
     *
     * See [Create poll](https://getstream.github.io/protocol/#/product%3Achat/CreatePoll).
     *
     * @param body Body holding the poll properties to be created.
     *
     * @return The poll response.
     */
    @POST("/polls")
    fun createPoll(@Body body: CreatePollRequest): RetrofitCall<PollResponse>

    // MISSING: Update poll

    /**
     * Deletes a poll.
     *
     * See [Delete poll](https://getstream.github.io/protocol/#/product%3Achat/DeletePoll).
     *
     * @param pollId The ID of the poll to delete.
     */
    @DELETE("/polls/{poll_id}")
    fun deletePoll(@Path("poll_id") pollId: String): RetrofitCall<CompletableResponse>

    /**
     * Retrieves a poll by its ID.
     *
     * See [Get poll](https://getstream.github.io/protocol/#/product%3Achat/GetPoll).
     *
     * @param pollId The poll ID.
     *
     * @return The poll response.
     */
    @GET("/polls/{poll_id}")
    fun getPoll(@Path("poll_id") pollId: String): RetrofitCall<PollResponse>

    /**
     * Updates a poll.
     *
     * See [Partial update poll](https://getstream.github.io/protocol/#/product%3Achat/UpdatePollPartial).
     *
     * @param pollId The poll ID.
     * @param body The poll update request.
     *
     * @return The poll response.
     */
    @PATCH("/polls/{poll_id}")
    fun partialUpdatePoll(
        @Path("poll_id") pollId: String,
        @Body body: PartialUpdatePollRequest,
    ): RetrofitCall<PollResponse>

    /**
     * Create a new option for a poll.
     *
     * See [Create poll option](https://getstream.github.io/protocol/#/product%3Achat/CreatePollOption).
     *
     * @param pollId The poll ID.
     * @param body The create poll option request.
     *
     * @return The suggest poll option response.
     */
    @POST("/polls/{poll_id}/options")
    fun createPollOption(
        @Path("poll_id") pollId: String,
        @Body body: CreatePollOptionRequest,
    ): RetrofitCall<PollOptionResponse>

    /**
     * Update an option for a poll.
     *
     * See [Update poll option](https://getstream.github.io/protocol/#/product%3Achat/UpdatePollOption).
     *
     * @param pollId The poll ID.
     * @param body The update poll option request.
     */
    @PUT("/polls/{poll_id}/options")
    fun updatePollOption(
        @Path("poll_id") pollId: String,
        @Body body: UpdatePollOptionRequest,
    ): RetrofitCall<PollOptionResponse>

    /**
     * Deletes an option from a poll.
     *
     * See [Delete poll option](https://getstream.github.io/protocol/#/product%3Achat/DeletePollOption).
     *
     * @param pollId The poll ID.
     * @param optionId The option ID.
     */
    @DELETE("/polls/{poll_id}/options/{option_id}")
    fun deletePollOption(
        @Path("poll_id") pollId: String,
        @Path("option_id") optionId: String,
    ): RetrofitCall<CompletableResponse>

    // MISSING: Get poll option

    // MISSING: Query votes

    /**
     * Queries polls based on the provided criteria.
     *
     * See: [Query polls](https://getstream.github.io/protocol/#/product%3Achat/QueryPolls).
     *
     * @param body The query polls request.
     */
    @POST("/polls/query")
    fun queryPolls(@Body body: QueryPollsRequest): RetrofitCall<QueryPollsResponse>

    /**
     * Casts a vote on a poll.
     *
     * See: [Cast poll vote](https://getstream.github.io/protocol/#/product%3Achat/CastPollVote).
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
     * Deletes a vote on a poll.
     *
     * See: [Delete poll vote](https://getstream.github.io/protocol/#/product%3Achat/DeletePollVote).
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
}
