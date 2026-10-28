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

package io.getstream.chat.android.models

import androidx.compose.runtime.Immutable

/**
 * Where a [ScheduledMessage] is, from scheduling until it is sent. More statuses can be added in a minor version, so a
 * `when` on it needs an `else` branch.
 */
@Immutable
public abstract class ScheduledMessageStatus internal constructor() {

    /**
     * The raw value of the status.
     */
    public abstract val value: String

    /**
     * The latest changes are not on the server yet: the attachments are uploading, the device is offline, or the
     * changes are being sent.
     */
    public data object PreparingToSchedule : ScheduledMessageStatus() {
        override val value: String = "preparing_to_schedule"
    }

    /**
     * The server has the latest changes and sends the message at [ScheduledMessage.scheduledAt].
     */
    public data object Scheduled : ScheduledMessageStatus() {
        override val value: String = "scheduled"
    }

    /**
     * The server is sending the message, at its date or after a send now, including while it retries a failed attempt.
     * It can no longer be edited, rescheduled or sent now.
     */
    public data object Sending : ScheduledMessageStatus() {
        override val value: String = "sending"
    }

    /**
     * The message could not be scheduled or sent. See [ScheduledMessage.failure].
     */
    public data object Failed : ScheduledMessageStatus() {
        override val value: String = "failed"
    }

    /**
     * The message was sent. Only the scheduled message of a `scheduled_message.sent` event has it.
     */
    public data object Sent : ScheduledMessageStatus() {
        override val value: String = "sent"
    }

    /**
     * The message was deleted, so it is never sent. Only the scheduled message of a `scheduled_message.deleted` event
     * has it.
     */
    public data object Canceled : ScheduledMessageStatus() {
        override val value: String = "canceled"
    }

    /**
     * A status this version of the SDK does not know.
     *
     * @property value The raw value of the status.
     */
    public data class Unknown(override val value: String) : ScheduledMessageStatus()
}
