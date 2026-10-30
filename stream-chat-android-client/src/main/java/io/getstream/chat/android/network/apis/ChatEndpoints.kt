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
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

internal interface ChatEndpoints {

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
}
