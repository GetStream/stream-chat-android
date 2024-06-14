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
import io.getstream.chat.android.client.api2.model.requests.PollRequest
import io.getstream.chat.android.client.api2.model.requests.PollUpdateRequest
import io.getstream.chat.android.client.api2.model.requests.PollVoteRequest
import io.getstream.chat.android.client.api2.model.response.PollResponse
import io.getstream.chat.android.client.api2.model.response.PollVoteResponse
import io.getstream.chat.android.client.call.RetrofitCall
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

@AuthenticatedApi
internal interface PollsApi {

    /**
     * Creates a new poll.
     *
     * @param pollRequest The poll request.
     *
     * @return The poll response.
     */
    @POST("/polls")
    fun createPoll(@Body pollRequest: PollRequest): RetrofitCall<PollResponse>

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
}
