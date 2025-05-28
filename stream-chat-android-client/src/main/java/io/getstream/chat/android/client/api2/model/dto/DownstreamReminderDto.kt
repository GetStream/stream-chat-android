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

package io.getstream.chat.android.client.api2.model.dto

import com.squareup.moshi.JsonClass
import java.util.Date

/**
 * Model representing the API model for message reminders.
 *
 * @property remind_at The date when the reminder should be sent.
 * @property channel_cid The ID of the channel in which the message is.
 * @property channel The channel in which the message is.
 * @property message_id The ID of the message for which the reminder is set.
 * @property message The message for which the reminder is set.
 * @property created_at The date when the reminder was created.
 * @property updated_at The date when the reminder was last updated.
 */
@JsonClass(generateAdapter = true)
internal data class DownstreamReminderDto(
    val remind_at: Date?,
    val channel_cid: String,
    val channel: DownstreamChannelDto?,
    val message_id: String,
    val message: DownstreamMessageDto?,
    val created_at: Date,
    val updated_at: Date,
)

/**
 * Model holding limited data about a message reminder.
 *
 * @property remind_at The date when the reminder should be sent.
 * @property created_at The date when the reminder was created.
 * @property updated_at The date when the reminder was last updated.
 */
@JsonClass(generateAdapter = true)
internal data class DownstreamReminderInfoDto(
    val remind_at: Date?,
    val created_at: Date,
    val updated_at: Date,
)
