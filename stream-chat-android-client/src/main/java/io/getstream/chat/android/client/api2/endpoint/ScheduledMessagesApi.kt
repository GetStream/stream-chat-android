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

package io.getstream.chat.android.client.api2.endpoint

import io.getstream.chat.android.client.api.AuthenticatedApi
import io.getstream.chat.android.client.call.RetrofitCall
import io.getstream.chat.android.network.models.DeleteScheduledMessageResponse
import io.getstream.chat.android.network.models.QueryScheduledMessagesRequest
import io.getstream.chat.android.network.models.QueryScheduledMessagesResponse
import io.getstream.chat.android.network.models.SendScheduledMessageNowResponse
import io.getstream.chat.android.network.models.UpdateScheduledMessageRequest
import io.getstream.chat.android.network.models.UpdateScheduledMessageResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Endpoints of the current user's scheduled messages. Scheduling a message goes through the send message endpoint,
 * with a `scheduled_at` date.
 */
@AuthenticatedApi
internal interface ScheduledMessagesApi {

    /**
     * Queries the scheduled messages of the current user.
     *
     * @param body The filter, sort and pagination of the query.
     */
    @POST("/api/v2/chat/scheduled_messages/query")
    fun queryScheduledMessages(
        @Body body: QueryScheduledMessagesRequest,
    ): RetrofitCall<QueryScheduledMessagesResponse>

    /**
     * Updates the content of a scheduled message, when it is sent, or both.
     *
     * @param id The ID of the scheduled message.
     * @param body The new content and date. Leaving out `message` or `scheduled_at` keeps it; a `message` replaces
     * the whole content.
     */
    @PATCH("/api/v2/chat/scheduled_messages/{id}")
    fun updateScheduledMessage(
        @Path("id") id: String,
        @Body body: UpdateScheduledMessageRequest,
    ): RetrofitCall<UpdateScheduledMessageResponse>

    /**
     * Deletes a scheduled message, so it is never sent.
     *
     * @param id The ID of the scheduled message.
     */
    @DELETE("/api/v2/chat/scheduled_messages/{id}")
    fun deleteScheduledMessage(
        @Path("id") id: String,
    ): RetrofitCall<DeleteScheduledMessageResponse>

    /**
     * Sends a scheduled message right away.
     *
     * @param id The ID of the scheduled message.
     */
    @POST("/api/v2/chat/scheduled_messages/{id}/send_now")
    fun sendScheduledMessageNow(
        @Path("id") id: String,
        @Body body: Map<String, String> = emptyMap(),
    ): RetrofitCall<SendScheduledMessageNowResponse>
}
