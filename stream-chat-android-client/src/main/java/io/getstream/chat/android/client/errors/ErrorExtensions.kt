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

package io.getstream.chat.android.client.errors

import io.getstream.chat.android.models.ScheduledMessageFailure.ServerRejectedScheduling
import io.getstream.chat.android.models.SyncStatus
import io.getstream.result.Error

/** The backend error code of a request for a resource that does not exist. */
private const val DOES_NOT_EXIST_ERROR_CODE = 16

/**
 * Checks whether this error is the backend rejection of a message whose id already exists.
 *
 * The backend enforces message id uniqueness and rejects a repeated send of the same message with
 * a validation error. Receiving it means an earlier attempt already delivered the message (for
 * example, a send retried after its response was lost on a connection drop), so callers should
 * treat the send as successful instead of marking the message as failed.
 */
internal fun Error.isDuplicateMessageError(): Boolean =
    this is Error.NetworkError &&
        serverErrorCode == ChatErrorCode.VALIDATION_ERROR.code &&
        message.contains("a message with ID") &&
        message.contains("already exists")

/**
 * Checks whether this error is the backend rejection of a delete whose target no longer exists for the current
 * user: the message is already deleted or does not exist, the channel was deleted, or the user left it.
 *
 * Either way the message is gone for the user, which is what the delete asked for, so callers should treat the
 * delete as successful instead of retrying it.
 */
internal fun Error.isMessageGoneError(): Boolean =
    this is Error.NetworkError && isStatusNotFound() && serverErrorCode == DOES_NOT_EXIST_ERROR_CODE

/**
 * The [SyncStatus] of a delete that failed with this error: [SyncStatus.COMPLETED] when the message is already gone,
 * else [SyncStatus.SYNC_NEEDED] so the delete is retried.
 */
internal fun Error.deleteFailureSyncStatus(): SyncStatus =
    if (isMessageGoneError()) SyncStatus.COMPLETED else SyncStatus.SYNC_NEEDED

/** The scheduled message rejections by their backend error code. */
@Suppress("MagicNumber") // The numbers are the backend codes, listed once here.
private val SCHEDULED_MESSAGE_REJECTION_REASONS = mapOf(
    114 to ServerRejectedScheduling.Reason.ScheduledMessagesDisabled,
    115 to ServerRejectedScheduling.Reason.ScheduledAtTooSoon,
    116 to ServerRejectedScheduling.Reason.ScheduledAtTooFar,
    117 to ServerRejectedScheduling.Reason.ScheduledLimitChannel,
    118 to ServerRejectedScheduling.Reason.ScheduledLimitUser,
    119 to ServerRejectedScheduling.Reason.ScheduledMessageIdTaken,
    120 to ServerRejectedScheduling.Reason.ScheduledMessageNotEditable,
    121 to ServerRejectedScheduling.Reason.ScheduledMessageSending,
    122 to ServerRejectedScheduling.Reason.ScheduledMessageConflict,
    123 to ServerRejectedScheduling.Reason.ScheduledMessageAlreadySent,
    124 to ServerRejectedScheduling.Reason.ScheduledMessageCanceled,
)

/** The reasons the server rejects a scheduled message request with. */
internal val scheduledMessageRejectionReasons: Collection<ServerRejectedScheduling.Reason>
    get() = SCHEDULED_MESSAGE_REJECTION_REASONS.values

/**
 * The reason the server rejected a scheduled message request with this error, or null when the error is not one of the
 * scheduled message rejections. The reason is read from the numeric code: [Error.NetworkError] does not keep the
 * reason name the server also sends.
 */
internal fun Error.scheduledMessageRejectionReason(): ServerRejectedScheduling.Reason? =
    (this as? Error.NetworkError)?.let { SCHEDULED_MESSAGE_REJECTION_REASONS[it.serverErrorCode] }
