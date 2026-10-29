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

package io.getstream.chat.android.compose.sample.feature.scheduled

import androidx.annotation.StringRes
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.models.ScheduledMessage
import io.getstream.chat.android.models.ScheduledMessageFailure
import io.getstream.chat.android.models.ScheduledMessageFailure.LocalDeviceFailedScheduling
import io.getstream.chat.android.models.ScheduledMessageFailure.ServerFailedPublishing
import io.getstream.chat.android.models.ScheduledMessageFailure.ServerRejectedScheduling
import io.getstream.chat.android.models.ScheduledMessageStatus

/**
 * An action the user can take on a scheduled message.
 */
internal enum class ScheduledMessageAction {
    Edit,
    Reschedule,
    SendNow,
    Retry,

    /** Copies the text of a message that can only be deleted, as its channel was deleted. */
    CopyText,

    /** Edits a message the server failed to send in the composer, and schedules it again as a new message. */
    ScheduleAgain,
    Delete,
}

/**
 * The actions the status of the scheduled message allows. While the server sends it, nothing can be done. A message
 * the server failed to send can only be scheduled again as a new message, or deleted, and only copied or deleted when
 * its channel was deleted. A message the server can no longer change can only be deleted.
 */
internal val ScheduledMessage.actions: List<ScheduledMessageAction>
    get() = when (status) {
        ScheduledMessageStatus.Sending -> emptyList()
        ScheduledMessageStatus.Scheduled,
        ScheduledMessageStatus.PreparingToSchedule,
        -> EditableActions
        ScheduledMessageStatus.Failed -> failure.failedActions()
        else -> listOf(ScheduledMessageAction.Delete)
    }

private fun ScheduledMessageFailure?.failedActions(): List<ScheduledMessageAction> = when (this) {
    is LocalDeviceFailedScheduling -> listOf(ScheduledMessageAction.Retry) + EditableActions
    is ServerRejectedScheduling -> when (reason) {
        ServerRejectedScheduling.Reason.ScheduledMessageSending,
        ServerRejectedScheduling.Reason.ScheduledMessageNotEditable,
        -> listOf(ScheduledMessageAction.Delete)
        else -> listOfNotNull(ScheduledMessageAction.Retry.takeIf { isRetryable }) + EditableActions
    }
    is ServerFailedPublishing -> if (reason == ServerFailedPublishing.Reason.ChannelDeleted) {
        listOf(ScheduledMessageAction.CopyText, ScheduledMessageAction.Delete)
    } else {
        listOf(ScheduledMessageAction.ScheduleAgain, ScheduledMessageAction.Delete)
    }
    else -> listOf(ScheduledMessageAction.Delete)
}

private val EditableActions = listOf(
    ScheduledMessageAction.Edit,
    ScheduledMessageAction.Reschedule,
    ScheduledMessageAction.SendNow,
    ScheduledMessageAction.Delete,
)

/**
 * The text that says why the message failed, or null to show the message of the server.
 */
@get:StringRes
internal val ScheduledMessageFailure.description: Int?
    get() = when (this) {
        is ServerRejectedScheduling -> reason?.description
        is ServerFailedPublishing -> reason?.description ?: R.string.scheduled_messages_failed_to_send
        is LocalDeviceFailedScheduling -> when (reason) {
            LocalDeviceFailedScheduling.Reason.AttachmentUploadFailed ->
                R.string.scheduled_messages_failure_attachment_upload
            else -> R.string.scheduled_messages_failure_request
        }
        else -> R.string.scheduled_messages_failed_to_send
    }

private val ServerRejectedScheduling.Reason.description: Int?
    get() = when (this) {
        ServerRejectedScheduling.Reason.ScheduledLimitChannel -> R.string.scheduled_messages_failure_limit_channel
        ServerRejectedScheduling.Reason.ScheduledLimitUser -> R.string.scheduled_messages_failure_limit_user
        ServerRejectedScheduling.Reason.ScheduledAtTooSoon -> R.string.scheduled_messages_failure_too_soon
        ServerRejectedScheduling.Reason.ScheduledAtTooFar -> R.string.scheduled_messages_failure_too_far
        ServerRejectedScheduling.Reason.ScheduledMessagesDisabled -> R.string.scheduled_messages_failure_disabled
        ServerRejectedScheduling.Reason.ScheduledMessageSending -> R.string.scheduled_messages_failure_sending
        ServerRejectedScheduling.Reason.ScheduledMessageConflict -> R.string.scheduled_messages_failure_conflict
        else -> null
    }

private val ServerFailedPublishing.Reason.description: Int?
    get() = when (this) {
        ServerFailedPublishing.Reason.Banned -> R.string.scheduled_messages_failure_banned
        ServerFailedPublishing.Reason.ChannelDeleted -> R.string.scheduled_messages_failure_channel_deleted
        ServerFailedPublishing.Reason.ChannelFrozen -> R.string.scheduled_messages_failure_channel_frozen
        ServerFailedPublishing.Reason.ModerationBlocked -> R.string.scheduled_messages_failure_moderation
        ServerFailedPublishing.Reason.NoPermission -> R.string.scheduled_messages_failure_no_permission
        ServerFailedPublishing.Reason.InvalidMessage -> R.string.scheduled_messages_failure_invalid_message
        else -> null
    }
