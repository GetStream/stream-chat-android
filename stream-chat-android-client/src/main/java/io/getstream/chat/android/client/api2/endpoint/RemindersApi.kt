/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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
import io.getstream.chat.android.client.api2.model.requests.QueryRemindersRequest
import io.getstream.chat.android.client.api2.model.requests.ReminderRequest
import io.getstream.chat.android.client.api2.model.response.CompletableResponse
import io.getstream.chat.android.client.api2.model.response.QueryRemindersResponse
import io.getstream.chat.android.client.api2.model.response.ReminderResponse
import io.getstream.chat.android.client.call.RetrofitCall
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Defines the endpoints related to message reminders.
 */
@AuthenticatedApi
internal interface RemindersApi {

    /**
     * Queries the reminders.
     *
     * @param body The request body containing the query parameters.
     */
    @POST("/reminders/query")
    fun queryReminders(
        @Body body: QueryRemindersRequest,
    ): RetrofitCall<QueryRemindersResponse>

    /**
     * Creates a reminder for a message.
     *
     * @param messageId The ID of the message to create a reminder for.
     * @param body The request body containing the reminder details.
     */
    @POST("/messages/{id}/reminders")
    fun createReminder(
        @Path("id") messageId: String,
        @Body body: ReminderRequest,
    ): RetrofitCall<ReminderResponse>

    /**
     * Updates an existing reminder for a message.
     *
     * @param messageId The ID of the message to update the reminder for.
     * @param body The request body containing the updated reminder details.
     */
    @PATCH("/messages/{id}/reminders")
    fun updateReminder(
        @Path("id") messageId: String,
        @Body body: ReminderRequest,
    ): RetrofitCall<ReminderResponse>

    /**
     * Deletes a reminder for a message.
     *
     * @param messageId The ID of the message to delete the reminder for.
     */
    @DELETE("/messages/{id}/reminders")
    fun deleteReminder(
        @Path("id") messageId: String,
    ): RetrofitCall<CompletableResponse>
}
